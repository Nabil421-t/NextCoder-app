/**
 * examService.js
 *
 * Thin wrapper around the exam/contest endpoints exposed by
 * StudentExamController. Drop this into your existing services
 * folder — if you already have an axios instance with baseURL +
 * JWT interceptor set up (e.g. `api.js`), swap the `fetch` calls
 * below for that instance instead of duplicating auth headers here.
 */

const API_BASE = 'http://localhost:8083/api';

function authHeaders() {
  const token = localStorage.getItem('token'); // adjust to your auth storage key
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function handle(res) {
  if (!res.ok) {
    const body = await res.text().catch(() => '');
    throw new Error(`Request failed (${res.status}): ${body || res.statusText}`);
  }
  return res.json();
}

/** GET /api/exams/allExam -> ExamSummaryResponse[] */
export async function fetchAllExams() {
  const res = await fetch(`${API_BASE}/exams/allExam`, {
    headers: { ...authHeaders() },
  });
  return handle(res);
}

/** GET /api/exams/{examId} -> ExamDetailResponse */
export async function fetchExamDetail(examId) {
  const res = await fetch(`${API_BASE}/exams/${examId}`, {
    headers: { ...authHeaders() },
  });
  return handle(res);
}

/** POST /api/exams/{examId}/start -> StartExamResponse */
export async function startExam(examId) {
  const res = await fetch(`${API_BASE}/exams/${examId}/start`, {
    method: 'POST',
    headers: { ...authHeaders() },
  });
  return handle(res);
}

/**
 * POST /api/exams -> ExamDetailResponse (admin only)
 * Body must match CreateExamRequest exactly:
 *   { title, description, durationMinutes, startTime, passingMarks, problems: [{ problemId, score }] }
 * `problems` must contain EXACTLY 3 entries (enforced by @Size(min=3,max=3)
 * on the backend DTO) — the form below enforces the same before submitting.
 *
 * NOTE: adjust this path if your AdminExamController maps the create
 * endpoint somewhere other than the plain collection route.
 */
export async function createExam(payload) {
  const idempotencyKey = crypto.randomUUID();
  const res = await fetch(`${API_BASE}/admin/exams`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() 
      ,"Idempotency-Key": idempotencyKey,
    },
    body: JSON.stringify(payload),
  });
  return handle(res);
}
