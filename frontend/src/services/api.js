const BASE_URL = 'http://localhost:8083/api';

function getToken() {
  return localStorage.getItem('token');
}
function authHeaders() {
  const token = getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function handleResponse(res) {
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    // Spring's @Valid validation errors usually come back as
    // { message, errors: { fieldName: "reason" } } or { message, fieldErrors: [...] }
    // Log the FULL body so we can see exactly which field failed and why.
    console.error('❌ API error response:', JSON.stringify(data, null, 2));

    const fieldErrors = data.errors || data.fieldErrors || data.validationErrors;
    const detail = fieldErrors
      ? Object.entries(fieldErrors).map(([k, v]) => `${k}: ${v}`).join(', ')
      : (data.message || data.error || `HTTP ${res.status}`);

    throw new Error(detail);
  }
  return data;
}

// ── Auth ────────────────────────────────────────────
export async function registerUser(payload) {
  const res = await fetch(`${BASE_URL}/users/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function loginUser(payload) {
  const res = await fetch(`${BASE_URL}/users/login`, {
    method: 'POST',
    credentials: "include",
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  const response = await handleResponse(res);
  if (response?.data?.accessToken) {
    localStorage.setItem('token', response.data.accessToken);
  }
  return response;
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
}

/**
 * Decodes a JWT and returns the payload object.
 */
function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window.atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

/**
 * Extracts userId from the stored token.
 */
export function getUserIdFromToken() {
  const token = getToken();
  if (!token) return null;

  const payload = parseJwt(token);
  // Replace 'sub' or 'userId' with the actual key used in your JWT claims
  const userId = payload?.sub || payload?.userId || null;
  return userId;
}
export function getRole() {
  const token = getToken();
  if (!token) {
    return null;
  }
  const payload = parseJwt(token);
  const role = payload?.role;
  console.log("His role is ", role);
  return role;
}
/**
 * Extracts the user's role(s) from the stored JWT, if present.
 * Adjust the claim keys below if your backend's JwtService names the
 * role claim differently (e.g. purely "role" vs a Spring Security
 * "authorities" list of { authority: "ROLE_ADMIN" } objects).
 */
export function getUserRoleFromToken() {
  const token = getToken();
  console.log("Your token is", token);
  if (!token) return null;

  const payload = parseJwt(token);
  console.log("your payload is", payload);
  const raw = payload?.role || payload?.roles || payload?.authorities || payload?.scope;
  console.log("raw information is", raw);
  if (!raw) return null;

  if (Array.isArray(raw)) {
    return raw.map((r) => (typeof r === 'string' ? r : r?.authority || '')).join(',');
  }
  return String(raw);
}

/**
 * Client-side check only — this hides/shows the admin UI, it does NOT
 * enforce anything. The real gate must be a @PreAuthorize("hasRole('ADMIN')")
 * (or similar) on the backend create-exam endpoint.
 */
export function isAdmin() {
  const role = getUserRoleFromToken();
  console.log("His role is ", role);
  return !!role && role.toUpperCase().includes('ADMIN');
}
// ── Problems ────────────────────────────────────────

/**
 * GET /api/problems/dashboard/{userId}?page=0&size=20
 * Returns PagedResponse<ProblemDashboardResponse>
 */
export async function getDashboard(userId, page = 0, size = 20) {
  const res = await fetch(
    `${BASE_URL}/problems/dashboard/${userId}?page=${page}&size=${size}`,
    { headers: authHeaders() }
  );
  return handleResponse(res); // ApiResponse<PagedResponse<ProblemDashboardResponse>>
}

/**
 * GET /api/problems/{id}
 * Returns ApiResponse<ProblemResponse>
 */
export async function getProblemById(id) {
  const res = await fetch(`${BASE_URL}/problems/${id}`, {
    headers: authHeaders(),
  });
  return handleResponse(res);
}

/**
 * POST /api/problems  (admin)
 * Returns ApiResponse<ProblemResponse>
 */
export async function createProblem(payload) {
  const res = await fetch(`${BASE_URL}/problems`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

/**
 * DELETE /api/problems/{id}  (admin)
 */
export async function deleteProblem(id) {
  const res = await fetch(`${BASE_URL}/problems/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });
  return handleResponse(res);
}

// ── Submissions ────────────────────────────────────────

/**
 * POST /api/submissions
 * Body (SubmitCodeRequest) — matches the DTO exactly:
 *   { problemId, sourceCode, language, idempotency_key }
 * Returns ApiResponse<SubmissionResponse>
 * (userId is taken from the JWT on the backend via SecurityContextHelper,
 *  so it must NOT be included in the payload.)
 *
 * idempotency_key prevents duplicate submissions if the request is retried
 * (e.g. on network blip / double-click). A fresh UUID per submit attempt
 * is correct — it should NOT be reused across different code submissions.
 */
export async function submitCode({ problemId, sourceCode, language }) {
  const payload = {
    problemId,
    sourceCode,
    language,
    idempotency_key: crypto.randomUUID(),
  };

  const res = await fetch(`${BASE_URL}/submissions`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res); // ApiResponse<SubmissionResponse>
}

/**
 * GET /api/submissions/{submissionId}
 * Returns ApiResponse<SubmissionDetailResponse>
 * Use this to poll for the judging result after submitCode() returns.
 */
export async function getSubmissionDetail(submissionId) {
  const res = await fetch(`${BASE_URL}/submissions/${submissionId}`, {
    headers: authHeaders(),
  });
  return handleResponse(res); // ApiResponse<SubmissionDetailResponse>
}

/**
 * SubmissionStatus enum values that mean "still being judged" — matches
 * com.cuet.dsa.enums.SubmissionStatus exactly:
 *   ACCEPTED, RUNNING, PENDING, WRONG_ANSWER, TIME_LIMIT_EXCEEDED,
 *   MEMORY_LIMIT_EXCEEDED, RUNTIME_ERROR, COMPILATION_ERROR,
 *   PRESENTATION_ERROR, OUTPUT_LIMIT_EXCEEDED, SKIPPED, QUEUE_FAILED,
 *   INTERNAL_ERROR
 * Only PENDING and RUNNING are "in progress" — every other value is final.
 */
export const SUBMISSION_PENDING_STATUSES = ['PENDING', 'RUNNING'];

/**
 * Final verdict values — matches com.cuet.dsa.enums.Verdict exactly:
 *   ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, MEMORY_LIMIT_EXCEEDED,
 *   RUNTIME_ERROR, COMPILATION_ERROR, PRESENTATION_ERROR,
 *   OUTPUT_LIMIT_EXCEEDED, SKIPPED, SYSTEM_ERROR
 */
export const VERDICT_LABELS = {
  ACCEPTED: 'Accepted',
  WRONG_ANSWER: 'Wrong Answer',
  TIME_LIMIT_EXCEEDED: 'Time Limit Exceeded',
  MEMORY_LIMIT_EXCEEDED: 'Memory Limit Exceeded',
  RUNTIME_ERROR: 'Runtime Error',
  COMPILATION_ERROR: 'Compilation Error',
  PRESENTATION_ERROR: 'Presentation Error',
  OUTPUT_LIMIT_EXCEEDED: 'Output Limit Exceeded',
  SKIPPED: 'Skipped',
  SYSTEM_ERROR: 'System Error',
};

/**
 * Polls GET /api/submissions/{submissionId} every `intervalMs` until the
 * submission's status is no longer PENDING/RUNNING, or until `timeoutMs`
 * elapses. Returns the final ApiResponse<SubmissionDetailResponse>.
 */
export async function pollSubmissionResult(
  submissionId,
  { intervalMs = 1000, timeoutMs = 25000 } = {}
) {
  const start = Date.now();

  while (Date.now() - start < timeoutMs) {
    const res = await getSubmissionDetail(submissionId);
    const rawStatus = res?.data?.status || res?.data?.verdict;
    const status = rawStatus ? String(rawStatus).toUpperCase() : '';

    if (status && !SUBMISSION_PENDING_STATUSES.includes(status)) {
      return res; // judging finished — verdict is final
    }

    await new Promise(r => setTimeout(r, intervalMs));
  }

  throw new Error('Judging timed out — please check submission history later.');
}
export async function runCode({ problemId, sourceCode, language }) {
  const payload = {
    problemId,
    sourceCode,
    language,
    idempotency_key: crypto.randomUUID(),
  };

  const res = await fetch(`${BASE_URL}/code-run`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}