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
    console.error('❌ Post API error response:', JSON.stringify(data, null, 2));
    const fieldErrors = data.errors || data.fieldErrors || data.validationErrors;
    const detail = fieldErrors
      ? Object.entries(fieldErrors).map(([k, v]) => `${k}: ${v}`).join(', ')
      : (data.message || data.error || `HTTP ${res.status}`);
    throw new Error(detail);
  }
  return data;
}

/**
 * POST /api/posts
 * Body: { postBody }
 */
export async function createPost(payload) {
  const res = await fetch(`${BASE_URL}/posts`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

/**
 * GET /api/posts?page=0&size=20
 * Returns ApiResponse<PagedResponse<PostResponse>>
 */
export async function getFeed(page = 0, size = 20) {
  const res = await fetch(`${BASE_URL}/posts?page=${page}&size=${size}`, {
    headers: authHeaders(),
  });
  return handleResponse(res);
}

/**
 * GET /api/posts/{postId}
 * Returns ApiResponse<PostResponse>
 */
export async function getPost(postId) {
  const res = await fetch(`${BASE_URL}/posts/${postId}`, {
    headers: authHeaders(),
  });
  return handleResponse(res);
}

/**
 * GET /api/posts/user/{userId}?page=0&size=20
 * Returns ApiResponse<PagedResponse<PostResponse>>
 */
export async function getUserPosts(userId, page = 0, size = 20) {
  const res = await fetch(`${BASE_URL}/posts/user/${userId}?page=${page}&size=${size}`, {
    headers: authHeaders(),
  });
  return handleResponse(res);
}

/**
 * PUT /api/posts/{postId}
 * Body: { postBody }
 */
export async function updatePost(postId, payload) {
  const res = await fetch(`${BASE_URL}/posts/${postId}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

/**
 * DELETE /api/posts/{postId}
 */
export async function deletePost(postId) {
  const res = await fetch(`${BASE_URL}/posts/${postId}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });
  return handleResponse(res);
}
