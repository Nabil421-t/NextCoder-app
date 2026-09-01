const API_BASE = 'http://localhost:8083/api';

function authHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function handle(res) {
  if (res.status === 204) return null;
  const isJson = res.headers.get('content-type')?.includes('application/json');
  const body = isJson ? await res.json().catch(() => null) : await res.text().catch(() => '');
  if (!res.ok) {
    const message = (isJson && (body?.message || body?.error)) || body || `Request failed (${res.status})`;
    throw new Error(message);
  }
  return body;
}

export async function fetchNotifications(userId) {
  const res = await fetch(`${API_BASE}/users/notifications/${userId}`, {
    headers: authHeaders(),
  });
  return handle(res);
}

export async function fetchUnreadNotifications(userId) {
  const res = await fetch(`${API_BASE}/users/${userId}/notifications/unread`, {
    headers: authHeaders(),
  });
  return handle(res);
}

export async function markNotificationAsRead(userId, notificationId) {
  const res = await fetch(
    `${API_BASE}/users/${userId}/notifications/${notificationId}/read`,
    { method: 'PATCH', headers: authHeaders() }
  );
  return handle(res);
}

export async function markAllNotificationsAsRead(userId) {
  const res = await fetch(`${API_BASE}/users/${userId}/notifications/read-all`, {
    method: 'PATCH',
    headers: authHeaders(),
  });
  return handle(res);
}

export async function hideNotification(userId, notificationId) {
  const res = await fetch(
    `${API_BASE}/users/${userId}/notifications/${notificationId}`,
    { method: 'DELETE', headers: authHeaders() }
  );
  return handle(res);
}
