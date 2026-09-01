import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import {
  getProblemById,
  logout,
  submitCode,
  runCode,
  pollSubmissionResult,
  SUBMISSION_PENDING_STATUSES,
  VERDICT_LABELS,
} from '../../services/api';
import './Problems.css';
import './Editor.css';

const DIFF_COLOR = {
  EASY:   { color: '#2fd67a', label: 'Easy' },
  MEDIUM: { color: '#ffb84d', label: 'Medium' },
  HARD:   { color: '#ff6b6b', label: 'Hard' },
};

// ─────────────────────────────────────────────────────────────────────────
// Keys MUST match com.cuet.dsa.enums.Language exactly — backend deserializes
// the `language` field straight into this enum, so any mismatch → 400.
// (Fixed: "Java" -> "JAVA" to match the all-caps convention used by every
// other key — this was silently 400ing every Java submission before.)
//
// IMPORTANT: JudgeEngine compiles and runs the submitted source as a
// STANDALONE PROGRAM — it writes raw text to stdin and reads stdout. It
// does NOT instantiate a `Solution` class or call a method on it. So the
// starter code below is a plain stdin -> stdout skeleton for every
// problem, not a LeetCode-style function stub (a function-stub starter
// like `vector<int> twoSum(vector<int>& nums, int target)` will compile
// to a program with no `main()`, or produce output that doesn't match
// what the judge's test-case runner actually expects).
// ─────────────────────────────────────────────────────────────────────────
const LANG_STARTERS = {
  PYTHON: () =>
`# Read input from stdin, write your answer to stdout.
# Example (Two Sum style): first line is the array, second line is the target.
#
# import sys
# data = sys.stdin.read().split('\\n')
# nums = list(map(int, data[0].split()))
# target = int(data[1])

`,
  JAVA: () =>
`import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Read input from stdin, write your answer to stdout via System.out
        
    }
}
`,
  CPP: () =>
`#include <bits/stdc++.h>
using namespace std;

int main() {
    // Read input from stdin, write your answer to stdout
    
    return 0;
}
`,
  JAVASCRIPT: () =>
`// Read input from stdin, write your answer to stdout
const lines = require('fs').readFileSync('/dev/stdin', 'utf8').split('\\n');

`,
};

// Friendly labels for the language buttons (display only — sent value is the enum key)
const LANG_LABELS = {
  PYTHON:     'Python',
  JAVA:       'Java',
  CPP:        'C++',
  JAVASCRIPT: 'JavaScript',
};

const LEFT_TABS = [
  { key: 'description', label: 'Description', icon: '📄', enabled: true },
  { key: 'note',        label: 'Note',        icon: '📝', enabled: false },
  { key: 'editorial',   label: 'Editorial',    icon: '📖', enabled: false },
  { key: 'solution',    label: 'Solutions',    icon: '🧪', enabled: true },
  { key: 'submissions', label: 'Submissions',  icon: '↺',  enabled: true },
];

export default function ProblemDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [problem, setProblem]   = useState(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');
  const [lang, setLang]         = useState('PYTHON');
  const [code, setCode]         = useState('');
  const [activeTab, setActiveTab] = useState('description'); // 'description' | 'solution' | 'submissions'
  const [bottomTab, setBottomTab] = useState('testcase'); // 'testcase' | 'result'
  const [activeCase, setActiveCase] = useState(0);
  const [runResult, setRunResult] = useState(null);
  const [running, setRunning]   = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitResult, setSubmitResult] = useState(null);
  const [topicsOpen, setTopicsOpen] = useState(false);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError('');
      try {
        const res = await getProblemById(id);
        setProblem(res.data);
        setCode(LANG_STARTERS[lang]?.() || '');
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (problem) setCode(LANG_STARTERS[lang]?.() || '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lang]);

  function handleLogout() { logout(); navigate('/login'); }

  function formatMemory(detail) {
    let kb = detail?.peakMemoryKb ?? detail?.memoryKb;
    if ((kb == null || kb === 0) && Array.isArray(detail?.results) && detail.results.length > 0) {
      const maxResMem = Math.max(...detail.results.map((r) => r.memoryKb || 0));
      if (maxResMem > 0) kb = maxResMem;
    }
    if (kb == null) return '—';
    if (kb >= 1024) {
      return `${(kb / 1024).toFixed(1)} MB`;
    }
    return `${kb} KB`;
  }

  function formatTime(detail) {
    let ms = detail?.runtimeMs ?? detail?.avgRuntimeMs;
    if ((ms == null || ms === 0) && Array.isArray(detail?.results) && detail.results.length > 0) {
      const maxResTime = Math.max(...detail.results.map((r) => r.runtimeMs || 0));
      if (maxResTime > 0) ms = maxResTime;
    }
    return ms != null ? `${ms}ms` : '—';
  }

  // "Run" executes against visible sample test cases only, for fast feedback.
  // Currently hits the same JudgeEngine as /submissions via /code-run, so the
  // response shape matches submitResult (verdict/score/time/memory) — not a
  // per-sample-case pass/fail array. If the backend later exposes per-case
  // results for Run, swap the render block below for a cases.map().
  async function handleRun() {
    setRunning(true);
    setRunResult(null);
    setSubmitResult(null);
    setBottomTab('result');
    try {
      const created = await runCode({
        problemId: Number(id),
        sourceCode: code,
        language: lang, // must be one of: CPP, JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, C, CSHARP, GO, RUST, KOTLIN, SWIFT
      });

      const submissionId = created?.data?.id;
      let detail = created?.data || {};

      const initialStatus = (detail.status || detail.verdict || '').toUpperCase();
      if (submissionId && (!initialStatus || SUBMISSION_PENDING_STATUSES.includes(initialStatus))) {
        const judged = await pollSubmissionResult(submissionId);
        detail = judged?.data || detail;
      }

      const rawKey = detail.verdict || detail.status || 'SYSTEM_ERROR';
      const verdictKey = String(rawKey).toUpperCase();

      const passedCount = detail.passedTestCases ?? detail.score;
      const totalCount = detail.totalTestCases;
      const scoreStr = (passedCount != null && totalCount != null) ? `${passedCount}/${totalCount}` : (passedCount ?? '—');

      setRunResult({
        verdict: VERDICT_LABELS[verdictKey] || verdictKey,
        isAccepted: verdictKey === 'ACCEPTED',
        score: scoreStr,
        time: formatTime(detail),
        memory: formatMemory(detail),
      });
    } catch (err) {
      setRunResult({
        verdict: err.message || 'Run failed',
        isAccepted: false,
        score: '—',
        time: '—',
        memory: '—',
      });
    } finally {
      setRunning(false);
    }
  }

  // "Submit" calls POST /api/submissions, then polls until status is final.
  async function handleSubmit() {
    setSubmitting(true);
    setSubmitResult(null);
    setRunResult(null);
    setBottomTab('result');
    try {
      const created = await submitCode({
        problemId: Number(id),
        sourceCode: code,
        language: lang, // must be one of: CPP, JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, C, CSHARP, GO, RUST, KOTLIN, SWIFT
      });

      const submissionId = created?.data?.id;
      let detail = created?.data || {};

      const initialStatus = (detail.status || detail.verdict || '').toUpperCase();
      if (submissionId && (!initialStatus || SUBMISSION_PENDING_STATUSES.includes(initialStatus))) {
        const judged = await pollSubmissionResult(submissionId);
        detail = judged?.data || detail;
      }

      const rawKey = detail.verdict || detail.status || 'SYSTEM_ERROR';
      const verdictKey = String(rawKey).toUpperCase();

      const passedCount = detail.passedTestCases ?? detail.score;
      const totalCount = detail.totalTestCases;
      const scoreStr = (passedCount != null && totalCount != null) ? `${passedCount}/${totalCount}` : (passedCount ?? '—');

      setSubmitResult({
        verdict: VERDICT_LABELS[verdictKey] || verdictKey,
        isAccepted: verdictKey === 'ACCEPTED',
        score: scoreStr,
        time: formatTime(detail),
        memory: formatMemory(detail),
      });
    } catch (err) {
      setSubmitResult({
        verdict: err.message || 'Submission failed',
        isAccepted: false,
        score: '—',
        time: '—',
        memory: '—',
      });
    } finally {
      setSubmitting(false);
    }
  }

  function handleReset() {
    setCode(LANG_STARTERS[lang]?.() || '');
    setRunResult(null);
    setSubmitResult(null);
  }

  if (loading) return (
    <div className="pw-root">
      <ProbNavbar onLogout={handleLogout} />
      <div className="pw-loading"><div className="spinner" /><span>Loading problem…</span></div>
    </div>
  );

  if (error) return (
    <div className="pw-root">
      <ProbNavbar onLogout={handleLogout} />
      <div className="pw-error">⚠ {error}<br /><Link to="/problems">← Back to problems</Link></div>
    </div>
  );

  const diff = DIFF_COLOR[problem?.difficultyLevel] || { color: '#9CA3AF', label: problem?.difficultyLevel };
  const cases = problem?.sampleTestCases || [];
  const patterns = problem?.patterns || []; // [{ id, name, priority }, …] — replaces old single patternName string

  return (
    <div className="pw-root">
      <ProbNavbar onLogout={handleLogout} />

      <div className="pw-panes">
        {/* ══════════ LEFT: description / solutions / submissions ══════════ */}
        <section className="pw-pane pw-left">
          <div className="pw-tabbar">
            <Link to="/problems" className="pw-tab-back" title="Back to problems">←</Link>
            {LEFT_TABS.map((t) => (
              <button
                key={t.key}
                className={`pw-tab ${activeTab === t.key ? 'is-active' : ''} ${!t.enabled ? 'is-disabled' : ''}`}
                onClick={() => t.enabled && setActiveTab(t.key)}
                disabled={!t.enabled}
              >
                <span className="pw-tab-icon">{t.icon}</span>
                {t.label}
              </button>
            ))}
          </div>

          <div className="pw-scroll">
            {activeTab === 'description' && (
              <>
                <h1 className="pw-title">
                  {problem?.id != null && <span className="pw-title-num">{problem.id}.</span>} {problem?.title}
                </h1>

                <div className="pw-chips">
                  <span className="pw-diff-badge" style={{ color: diff.color, background: `${diff.color}22` }}>
                    {diff.label}
                  </span>
                  {patterns.length > 0 && (
                    <span className="pw-chip">
                      {patterns.slice().sort((a, b) => (a.priority ?? 0) - (b.priority ?? 0))[0].name}
                    </span>
                  )}
                  {problem?.platform && <span className="pw-chip pw-chip--muted">{problem.platform}</span>}
                </div>

                <div className="pw-topics">
                  <button
                    type="button"
                    className="pw-topics-header"
                    onClick={() => setTopicsOpen((o) => !o)}
                    aria-expanded={topicsOpen}
                  >
                    <span className="pw-topics-header-left">
                      <span className="pw-topics-icon" aria-hidden="true">🏷</span>
                      <span>Topics</span>
                    </span>
                    <span className={`pw-topics-chevron ${topicsOpen ? 'is-open' : ''}`} aria-hidden="true">⌄</span>
                  </button>

                  {topicsOpen && (
                    <div className="pw-topics-body">
                      {patterns.length > 0 ? (
                        patterns
                          .slice()
                          .sort((a, b) => (a.priority ?? 0) - (b.priority ?? 0))
                          .map((pat) => (
                            <span key={pat.id} className="pw-chip">{pat.name}</span>
                          ))
                      ) : (
                        <span className="pw-topics-empty">No topics tagged for this problem.</span>
                      )}
                    </div>
                  )}
                </div>

                <p className="pw-desc">{problem?.description}</p>

                {cases.length > 0 && (
                  <div className="pw-examples">
                    {cases.map((tc, i) => (
                      <div key={tc.id ?? i} className="pw-example">
                        <div className="pw-example-title">Example {i + 1}:</div>
                        <div className="pw-example-block">
                          <div><strong>Input:</strong> {tc.input}</div>
                          <div><strong>Output:</strong> {tc.expectedOutput}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {problem?.constraints ? (
                  <div className="pw-constraints">
                    <h3>Constraints</h3>
                    <pre>{problem.constraints}</pre>
                  </div>
                ) : null}
              </>
            )}

            {activeTab === 'solution' && (
              <div className="pw-empty-state">
                <span className="pw-empty-icon">🔒</span>
                <p>Solutions unlock after you solve or attempt this problem.</p>
              </div>
            )}

            {activeTab === 'submissions' && (
              <div className="pw-empty-state">
                <span className="pw-empty-icon">↺</span>
                <p>No submissions yet. Solve the problem to see your history here.</p>
              </div>
            )}
          </div>
        </section>

        {/* ══════════ RIGHT: code editor + testcase/result panel ══════════ */}
        <section className="pw-pane pw-right">
          <div className="pw-code-header">
            <span className="pw-code-title"><span className="pw-code-title-icon">{'</>'}</span> Code</span>
          </div>

          <div className="pw-code-toolbar">
            <div className="pw-lang-select">
              {Object.keys(LANG_STARTERS).map((l) => (
                <button
                  key={l}
                  className={`pw-lang-btn ${lang === l ? 'is-active' : ''}`}
                  onClick={() => setLang(l)}
                >
                  {LANG_LABELS[l] || l}
                </button>
              ))}
            </div>
            <button className="pw-icon-btn" onClick={handleReset} title="Reset to starter code">↺</button>
          </div>

          <div className="pw-editor-wrap">
            <div className="pw-editor-gutter">
              {code.split('\n').map((_, i) => (
                <div key={i} className="pw-line-num">{i + 1}</div>
              ))}
            </div>
            <textarea
              className="pw-code-editor"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              spellCheck={false}
              autoCapitalize="off"
              autoCorrect="off"
            />
          </div>

          <div className="pw-run-toolbar">
            <button className="pw-btn pw-btn-ghost" onClick={handleRun} disabled={running}>
              {running ? <span className="pw-mini-spinner" /> : '▶'} Run
            </button>
            <button className="pw-btn pw-btn-primary" onClick={handleSubmit} disabled={submitting}>
              {submitting ? <span className="pw-mini-spinner" /> : '✓'} Submit
            </button>
          </div>

          <div className="pw-bottom-panel">
            <div className="pw-bottom-tabbar">
              <button
                className={`pw-bottom-tab ${bottomTab === 'testcase' ? 'is-active' : ''}`}
                onClick={() => setBottomTab('testcase')}
              >
                ✓ Testcase
              </button>
              <button
                className={`pw-bottom-tab ${bottomTab === 'result' ? 'is-active' : ''}`}
                onClick={() => setBottomTab('result')}
              >
                {'>_'} Test Result
              </button>
            </div>

            <div className="pw-bottom-content">
              {bottomTab === 'testcase' && (
                cases.length > 0 ? (
                  <>
                    <div className="pw-case-pills">
                      {cases.map((_, i) => (
                        <button
                          key={i}
                          className={`pw-case-pill ${activeCase === i ? 'is-active' : ''}`}
                          onClick={() => setActiveCase(i)}
                        >
                          Case {i + 1}
                        </button>
                      ))}
                    </div>
                    {cases[activeCase] && (
                      <div className="pw-case-detail">
                        <div className="pw-case-label">Input</div>
                        <pre className="pw-case-value">{cases[activeCase].input}</pre>
                        <div className="pw-case-label">Expected Output</div>
                        <pre className="pw-case-value">{cases[activeCase].expectedOutput}</pre>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="pw-empty-state pw-empty-state--compact">
                    <p>No sample test cases for this problem.</p>
                  </div>
                )
              )}

              {bottomTab === 'result' && (
                <>
                  {!runResult && !submitResult && !running && !submitting && (
                    <div className="pw-empty-state pw-empty-state--compact">
                      <p>Run or submit your code to see results here.</p>
                    </div>
                  )}

                  {(running || submitting) && !runResult && !submitResult && (
                    <div className="pw-empty-state pw-empty-state--compact">
                      <div className="spinner" />
                      <p>{running ? 'Running against sample cases…' : 'Judging your submission…'}</p>
                    </div>
                  )}

                  {runResult && (
                    <div className={`pw-submit-result ${runResult.isAccepted ? 'accepted' : 'wrong'}`}>
                      <div className="pw-submit-verdict">
                        {runResult.isAccepted ? '✓' : '✗'} {runResult.verdict}
                      </div>
                      <div className="pw-submit-meta">
                        Tests passed: {runResult.score} · Time: {runResult.time} · Memory: {runResult.memory}
                      </div>
                    </div>
                  )}

                  {submitResult && (
                    <div className={`pw-submit-result ${submitResult.isAccepted ? 'accepted' : 'wrong'}`}>
                      <div className="pw-submit-verdict">
                        {submitResult.isAccepted ? '✓' : '✗'} {submitResult.verdict}
                      </div>
                      <div className="pw-submit-meta">
                        Tests passed: {submitResult.score} · Time: {submitResult.time} · Memory: {submitResult.memory}
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}

function ProbNavbar({ onLogout }) {
  return (
    <header className="prob-nav pw-nav">
      <div className="prob-nav-inner">
        <Link to="/" className="prob-logo">
          <span className="logo-icon-p">{'</>'}</span>
          <span>Nextcoder</span>
        </Link>
        <nav className="prob-nav-links">
          <Link to="/problems" className="active">Problems</Link>
          <Link to="/contests">Contests</Link>
          <a href="#">Leaderboard</a>
        </nav>
        <div className="prob-nav-right">
          <button className="btn-logout" onClick={onLogout}>Log Out</button>
        </div>
      </div>
    </header>
  );
}