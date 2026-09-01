/**
 * dashboardService.js
 *
 * Thin wrapper around com.cuet.dsa.controller.DashboardController.
 * All endpoints are scoped under /api/dashboard/{userId}/... exactly as
 * mapped on the backend. Reuses the same BASE_URL / token convention as
 * services/api.js and services/examService.js.
 */

const BASE_URL = 'http://localhost:8083/api';

function authHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function handle(res) {
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    console.error('❌ Dashboard API error:', JSON.stringify(data, null, 2));
    throw new Error(data?.message || data?.error || `HTTP ${res.status}`);
  }
  return data;
}

/** GET /api/dashboard/{userId}/statistics -> DashboardStatisticsResponse */
export async function getStatistics(userId) {
  const res = await fetch(`${BASE_URL}/dashboard/${userId}/statistics`, {
    headers: authHeaders(),
  });
  return handle(res);
}

/** GET /api/dashboard/{userId}/category-progress -> CategoryProgressResponse[] */
export async function getCategoryProgress(userId) {
  const res = await fetch(`${BASE_URL}/dashboard/${userId}/category-progress`, {
    headers: authHeaders(),
  });
  return handle(res);
}

/** GET /api/dashboard/{userId}/category/{type} -> SolvedProblemResponse[] */
export async function getSolvedProblemsByCategory(userId, type) {
  const res = await fetch(`${BASE_URL}/dashboard/${userId}/category/${type}`, {
    headers: authHeaders(),
  });
  return handle(res);
}

/** GET /api/dashboard/{userId}/activity -> ActivityItemResponse[] */
export async function getActivity(userId) {
  const res = await fetch(`${BASE_URL}/dashboard/${userId}/activity`, {
    headers: authHeaders(),
  });
  return handle(res);
}


/** GET /api/dashboard/{userId}/status-distribution -> StatusDistributionResponse[] */
export async function getStatusDistribution(userId) {
  const res = await fetch(`${BASE_URL}/dashboard/${userId}/status-distribution`, {
    headers: authHeaders(),
  });
  return handle(res);
}

/** GET /api/dashboard/{userId}/platform-distribution -> PlatformDistributionResponse[] */
export async function getPlatformDistribution(userId) {
  const res = await fetch(`${BASE_URL}/dashboard/${userId}/platform-distribution`, {
    headers: authHeaders(),
  });
  return handle(res);
}

/** GET /api/dashboard/{userId}/recommendations -> RecommendationResponse[] */
export async function getRecommendations(userId) {
  const res = await fetch(`${BASE_URL}/dashboard/${userId}/recommendations`, {
    headers: authHeaders(),
  });
  return handle(res);
}

/**
 * Fetches everything the dashboard page needs in one call.
 * Each request is isolated with .catch so one failing endpoint
 * (e.g. an empty category table) doesn't blank out the whole page.
 */
export async function getFullDashboard(userId, year = new Date().getFullYear()) {
  const [
    statistics,
    categoryProgress,
    activity,
    statusDistribution,
    platformDistribution,
    recommendations,
  ] = await Promise.all([
    getStatistics(userId).catch(() => null),
    getCategoryProgress(userId).catch(() => []),
    getActivity(userId).catch(() => []),
    getStatusDistribution(userId).catch(() => []),
    getPlatformDistribution(userId).catch(() => []),
    getRecommendations(userId).catch(() => []),
  ]);

  return {
    statistics,
    categoryProgress,
    activity,
    statusDistribution,
    platformDistribution,
    recommendations,
  };
}
