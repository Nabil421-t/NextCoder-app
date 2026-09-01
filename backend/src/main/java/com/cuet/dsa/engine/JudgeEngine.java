package com.cuet.dsa.engine;


import com.cuet.dsa.entity.TestCase;
import com.cuet.dsa.enums.Language;
import com.cuet.dsa.enums.Verdict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * JudgeEngine — responsible for executing submitted code against test cases.
 *
 * CHANGE FROM PREVIOUS VERSION
 * ──────────────────────────────
 * Previously, an empty test-case list threw JudgeException. In the async
 * pipeline, any exception thrown here is caught by JudgeWorker as an
 * "unhandled error" and nack'd -> retry queue -> retried every 5s -> same
 * empty list -> same exception -> infinite retry loop -> eventually DLQ.
 *
 * Fix: judge() now NEVER throws for "no test cases". It returns a normal
 * JudgeResult with allPassed=false and an INTERNAL_ERROR-style message, so
 * JudgeWorker.finalizeSubmission() persists a final status (e.g.
 * INTERNAL_ERROR) and basicAck()s the message — the submission completes
 * instead of looping forever.
 *
 * FIX (this version) — COMPILE STEP TIMEOUT
 * ──────────────────────────────
 * compileProcess.waitFor() previously had NO timeout, unlike the run step.
 * If `docker run` for the compile stage hung (e.g. pulling an uncached
 * image, or a stuck docker daemon), the RabbitMQ consumer thread in
 * JudgeWorker would block forever: the message is never acked/nacked, the
 * submission stays RUNNING indefinitely, and the frontend eventually stops
 * polling and shows "Judging timed out — please check submission history
 * later." Compilation now uses the same bounded waitFor(timeoutMs, ...) as
 * the run step, and a hung/slow compile is reported as a COMPILATION_ERROR
 * ("Compilation timed out") instead of hanging the worker thread forever.
 *
 * REFACTOR NOTES (earlier version, still applies)
 * ──────────────────────────────
 * - Docker execution is now driven by two small helpers, compileCommand()
 *   and runCommand(), instead of one giant switch inlined in executeCode().
 * - Actual wall-clock runtime is measured with System.currentTimeMillis()
 *   around the run step; we no longer report the configured timeout as if
 *   it were the real runtime.
 * - Every temp working directory is deleted in a finally block, so failed
 *   or timed-out judge runs don't leak directories on disk.
 * - readCgroupPeakMemory() remains a placeholder returning 0 until real
 *   cgroup memory accounting is wired in.
 */
@Component
@Slf4j
public class JudgeEngine {

    private static final String MEMORY_MARKER = "__JUDGE_MEMORY_KB__:";

    @Value("${app.judge.timeout-ms:5000}")
    private long timeoutMs;

    @Value("${app.judge.memory-limit-mb:256}")
    private long memoryLimitMb;

    public JudgeResult judge(String sourceCode, Language language, List<TestCase> testCases) {

        // ── Guard: no test cases configured ─────────────────────────────────
        // Do NOT throw. Return a terminal result so the worker can ack and
        // finalize the submission as INTERNAL_ERROR instead of retry-looping.
        if (testCases == null || testCases.isEmpty()) {
            log.error("Judging aborted: no test cases configured for this problem");
            return JudgeResult.builder()
                    .allPassed(false)
                    .totalTestCases(0)
                    .passedTestCases(0)
                    .avgRuntimeMs(0L)
                    .peakMemoryKb(0L)
                    .errorMessage("No test cases configured for this problem")
                    .testCaseResults(new ArrayList<>())
                    .build();
        }

        log.info("Judging submission: language={}, testCases={}", language, testCases.size());

        List<JudgeResult.TestCaseResult> tcResults = new ArrayList<>();
        int passed = 0;

        for (TestCase tc : testCases) {
            JudgeResult.TestCaseResult result = executeOne(sourceCode, language, tc);
            tcResults.add(result);
            if (result.getVerdict() == Verdict.ACCEPTED) {
                passed++;
            } else {
                // Stop on first failure (LeetCode-style early exit)
                // Remove this break to run all cases (Codeforces-style)
                break;
            }
        }

        long avgRuntime = (long) tcResults.stream()
                .mapToLong(JudgeResult.TestCaseResult::getRuntimeMs)
                .average()
                .orElse(0);

        long peakMemory = tcResults.stream()
                .mapToLong(JudgeResult.TestCaseResult::getMemoryKb)
                .max()
                .orElse(0);

        String errorMessage = tcResults.stream()
                .filter(r -> r.getVerdict() != Verdict.ACCEPTED)
                .map(JudgeResult.TestCaseResult::getErrorTrace)
                .filter(e -> e != null && !e.isBlank())
                .findFirst()
                .orElse(null);

        boolean allPassed = (passed == testCases.size());

        return JudgeResult.builder()
                .allPassed(allPassed)
                .totalTestCases(testCases.size())
                .passedTestCases(passed)
                .avgRuntimeMs(avgRuntime)
                .peakMemoryKb(peakMemory)
                .errorMessage(errorMessage)
                .testCaseResults(tcResults)
                .build();
    }

    // ── Private: single test case execution ──────────────────────────────────

    private JudgeResult.TestCaseResult executeOne(String sourceCode,
                                                  Language language,
                                                  TestCase tc) {
        long startMs = System.currentTimeMillis();
        try {
            ExecutionResult exec = executeCode(sourceCode, language, tc.getInput());
            long elapsed = System.currentTimeMillis() - startMs;

            if (exec.isCompilationError()) {
                return JudgeResult.TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .verdict(Verdict.COMPILATION_ERROR)
                        .runtimeMs(elapsed)
                        .memoryKb(0L)
                        .actualOutput("")
                        .errorTrace(exec.getErrorOutput())
                        .build();
            }

            if (exec.isRuntimeError()) {
                return JudgeResult.TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .verdict(Verdict.RUNTIME_ERROR)
                        .runtimeMs(exec.getRuntimeMs())
                        .memoryKb(exec.getMemoryKb())
                        .actualOutput("")
                        .errorTrace(exec.getErrorOutput())
                        .build();
            }

            if (exec.getRuntimeMs() > timeoutMs) {
                return JudgeResult.TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .verdict(Verdict.TIME_LIMIT_EXCEEDED)
                        .runtimeMs(exec.getRuntimeMs())
                        .memoryKb(exec.getMemoryKb())
                        .actualOutput("")
                        .build();
            }

            if (exec.getMemoryKb() > memoryLimitMb * 1024) {
                return JudgeResult.TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .verdict(Verdict.MEMORY_LIMIT_EXCEEDED)
                        .runtimeMs(exec.getRuntimeMs())
                        .memoryKb(exec.getMemoryKb())
                        .actualOutput(exec.getStdout())
                        .build();
            }

            boolean correct = normalizedEquals(exec.getStdout(), tc.getExpectedOutput());

            return JudgeResult.TestCaseResult.builder()
                    .testCaseId(tc.getId())
                    .verdict(correct ? Verdict.ACCEPTED : Verdict.WRONG_ANSWER)
                    .runtimeMs(exec.getRuntimeMs())
                    .memoryKb(exec.getMemoryKb())
                    .actualOutput(exec.getStdout())
                    .build();

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startMs;
            log.error("Unexpected judge error for testCase {}: {}", tc.getId(), e.getMessage());
            return JudgeResult.TestCaseResult.builder()
                    .testCaseId(tc.getId())
                    .verdict(Verdict.RUNTIME_ERROR)
                    .runtimeMs(elapsed)
                    .memoryKb(0L)
                    .actualOutput("")
                    .errorTrace("Internal judge error: " + e.getMessage())
                    .build();
        }
    }

    // ── Execution pipeline: write source -> compile -> run -> cleanup ───────

    /**
     * Contract: never returns null; never throws for "expected" failure
     * modes — compilation errors, runtime errors, and timeouts are all
     * reported via ExecutionResult fields, not exceptions.
     */
    protected ExecutionResult executeCode(String sourceCode,
                                          Language language,
                                          String input)
            throws IOException, InterruptedException {

        Path workDir = Files.createTempDirectory("judge-");

        try {
            writeSource(workDir, sourceCode, language);

            // ---------- Compile ----------
            ProcessBuilder compilePb = compileCommand(language, workDir);

            if (compilePb != null) {
                Process compileProcess = compilePb.start();

                // FIX: bounded wait, same as the run step below. Previously
                // this was compileProcess.waitFor() with no timeout, which
                // could hang the JudgeWorker consumer thread forever (e.g.
                // docker pulling an uncached image), leaving the submission
                // stuck in RUNNING and never ack'd/nack'd.
                boolean compiled = compileProcess.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

                if (!compiled) {
                    compileProcess.destroyForcibly();
                    return ExecutionResult.builder()
                            .compilationError(true)
                            .errorOutput("Compilation timed out")
                            .build();
                }

                if (compileProcess.exitValue() != 0) {
                    String compileError =
                            new String(compileProcess.getErrorStream().readAllBytes());

                    return ExecutionResult.builder()
                            .compilationError(true)
                            .errorOutput(compileError)
                            .build();
                }
            }

            // ---------- Run ----------
            ProcessBuilder runPb = runCommand(language, workDir);
            Process process = runPb.start();

            String normalizedInput = normalizeInput(input);
            process.getOutputStream().write(normalizedInput.getBytes());
            process.getOutputStream().close();

            long runStart = System.currentTimeMillis();
            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            long runtimeMs = System.currentTimeMillis() - runStart;

            if (!finished) {
                process.destroyForcibly();

                return ExecutionResult.builder()
                        .runtimeError(true)
                        .runtimeMs(runtimeMs)
                        .errorOutput("Time Limit Exceeded")
                        .build();
            }

            String stdout = new String(process.getInputStream().readAllBytes());
            String stderrWithMemory = new String(process.getErrorStream().readAllBytes());
            long peakMemoryKb = extractPeakMemoryKb(stderrWithMemory);
            String stderr = removeMemoryMarker(stderrWithMemory);

            return ExecutionResult.builder()
                    .stdout(stdout)
                    .stderr(stderr)
                    .runtimeMs(runtimeMs)
                    .memoryKb(peakMemoryKb)
                    .exitCode(process.exitValue())
                    .runtimeError(process.exitValue() != 0)
                    .errorOutput(stderr)
                    .compilationError(false)
                    .build();

        } finally {
            deleteRecursively(workDir);
        }
    }

    /**
     * Builds the compile command for languages that need compilation.
     * Returns null when no compilation step is required (Python).
     */
    private ProcessBuilder compileCommand(Language language, Path workDir) {

        return switch (language) {
            case CPP -> new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", workDir.toAbsolutePath() + ":/code",
                    "-w", "/code",
                    imageFor(language),
                    "g++", "main.cpp", "-o", "main"
            );

            case JAVA -> new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", workDir.toAbsolutePath() + ":/code",
                    "-w", "/code",
                    imageFor(language),
                    "javac", "Main.java"
            );

            case PYTHON -> null;
        };
    }

    /**
     * Builds the sandboxed run command. Always includes --rm, -i,
     * --network=none, the configured memory limit and a single-core CPU cap.
     */
    private ProcessBuilder runCommand(Language language, Path workDir) {

        List<String> baseArgs = new ArrayList<>(List.of(
                "docker", "run",
                "--rm",
                "-i",
                "--memory=" + memoryLimitMb + "m",
                "--cpus=1",
                "--network=none",
                "-v", workDir.toAbsolutePath() + ":/code",
                "-w", "/code",
                imageFor(language)
        ));

        baseArgs.add("sh");
        baseArgs.add("-c");
        baseArgs.add(timedRunScript(language));

        return new ProcessBuilder(baseArgs);
    }

    private String timedRunScript(Language language) {
        String command = switch (language) {
            case CPP -> "./main";
            case JAVA -> "java Main";
            case PYTHON -> "python main.py";
        };

        return """
                if command -v /usr/bin/time >/dev/null 2>&1; then
                  /usr/bin/time -f '\\n%s%%M' %s
                else
                  %s
                fi
                """.formatted(MEMORY_MARKER, command, command);
    }

    private Path writeSource(Path workDir, String sourceCode, Language language) throws IOException {

        String fileName = switch (language) {
            case JAVA -> "Main.java";
            case CPP -> "main.cpp";
            case PYTHON -> "main.py";
        };

        Path sourceFile = workDir.resolve(fileName);
        Files.writeString(sourceFile, sourceCode);

        return sourceFile;
    }

    private String imageFor(Language language) {

        return switch (language) {
            case JAVA -> "openjdk:26";
            case CPP -> "gcc:latest";
            case PYTHON -> "python:3.12";
        };
    }

    /**
     * Converts LeetCode-style test-case input (e.g. "[2,7,11,15]\n9") into
     * plain whitespace-separated stdin tokens (e.g. "2 7 11 15\n9") that a
     * normal cin / Scanner / input().split() based solution can read.
     *
     * This is purely a runtime transformation — TestCase.input is never
     * mutated or re-saved. It is line-by-line so multi-line inputs keep
     * their line structure (each line still ends up on its own stdin line);
     * only the bracket/comma notation within a line is normalized:
     *   - '[' and ']' are stripped entirely
     *   - ',' becomes a single space (token separator)
     *   - repeated whitespace within a line is collapsed to one space
     *   - leading/trailing whitespace per line is trimmed
     *
     * Rows that are already plain (e.g. "1 1 0 1 1 1", "abcabcbb") pass
     * through unchanged aside from whitespace collapsing.
     */
    private String normalizeInput(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return "";
        }

        String[] lines = rawInput.replace("\r\n", "\n").split("\n", -1);
        StringBuilder normalized = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i]
                    .replace("[", "")
                    .replace("]", "")
                    .replace(",", " ")
                    .trim()
                    .replaceAll("\\s+", " ");

            normalized.append(line);
            if (i < lines.length - 1) {
                normalized.append("\n");
            }
        }

        return normalized.toString();
    }

    private long extractPeakMemoryKb(String stderr) {
        if (stderr == null) {
            return 1L;
        }

        int markerIndex = stderr.lastIndexOf(MEMORY_MARKER);
        if (markerIndex < 0) {
            return 1L;
        }

        int valueStart = markerIndex + MEMORY_MARKER.length();
        int valueEnd = valueStart;
        while (valueEnd < stderr.length() && Character.isDigit(stderr.charAt(valueEnd))) {
            valueEnd++;
        }

        if (valueEnd == valueStart) {
            return 1L;
        }

        try {
            return Math.max(1L, Long.parseLong(stderr.substring(valueStart, valueEnd)));
        } catch (NumberFormatException e) {
            return 1L;
        }
    }

    private String removeMemoryMarker(String stderr) {
        if (stderr == null || stderr.isBlank()) {
            return "";
        }

        int markerIndex = stderr.lastIndexOf(MEMORY_MARKER);
        if (markerIndex < 0) {
            return stderr;
        }

        int lineStart = stderr.lastIndexOf('\n', markerIndex);
        lineStart = lineStart < 0 ? markerIndex : lineStart;

        int lineEnd = stderr.indexOf('\n', markerIndex);
        lineEnd = lineEnd < 0 ? stderr.length() : lineEnd + 1;

        return (stderr.substring(0, lineStart) + stderr.substring(lineEnd)).stripTrailing();
    }

    /** Deletes a temp working directory and all of its contents, best-effort. */
    private void deleteRecursively(Path workDir) {
        try {
            Files.walk(workDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete temp path {}: {}", p, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to walk temp dir {} for cleanup: {}", workDir, e.getMessage());
        }
    }

    /**
     * Compares actual stdout against the stored expected output, tolerant
     * of LeetCode-style formatting differences (e.g. expected "[0,1]"
     * matching a plain-stdout program that printed "0 1"). Both sides are
     * canonicalized the same way input is normalized: brackets stripped,
     * commas turned into spaces, whitespace collapsed. This does NOT
     * reorder or reinterpret values — it only removes formatting noise.
     */
    private boolean normalizedEquals(String actual, String expected) {
        if (actual == null || expected == null) return false;
        return canonicalize(actual).equals(canonicalize(expected));
    }

    private String canonicalize(String s) {
        return s.replace("[", "")
                .replace("]", "")
                .replace(",", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    // ── Inner value object ───────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    public static class ExecutionResult {
        private String  stdout;
        private String  stderr;
        private int     exitCode;
        private long    runtimeMs;
        private long    memoryKb;
        private String  errorOutput;
        private boolean compilationError;
        private boolean runtimeError;
    }
}
