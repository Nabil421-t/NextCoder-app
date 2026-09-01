// Deterministic, aesthetically-paired palette — same category/status/platform
// name always resolves to the same color across reloads and across charts.
const PALETTE = [
  '#5B5BD6', // primary indigo
  '#14B8A6', // teal
  '#F59E0B', // amber
  '#F43F5E', // rose
  '#38BDF8', // sky
  '#7B2FBE', // accent purple
  '#84CC16', // lime
  '#FB923C', // orange
  '#EC4899', // pink
  '#22C55E', // green
];

const cache = new Map();

export function colorFor(key) {
  if (cache.has(key)) return cache.get(key);
  let hash = 0;
  for (let i = 0; i < key.length; i++) hash = (hash * 31 + key.charCodeAt(i)) >>> 0;
  const color = PALETTE[hash % PALETTE.length];
  cache.set(key, color);
  return color;
}

// "WRONG_ANSWER" -> "Wrong Answer", "CODEFORCES" -> "Codeforces"
export function titleCase(raw) {
  if (!raw) return '';
  return raw
    .toLowerCase()
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ');
}

const STATUS_COLORS = {
  ACCEPTED: '#22C55E',
  WRONG_ANSWER: '#F43F5E',
  TIME_LIMIT_EXCEEDED: '#F59E0B',
  MEMORY_LIMIT_EXCEEDED: '#F59E0B',
  RUNTIME_ERROR: '#FB923C',
  COMPILATION_ERROR: '#94A3B8',
  PENDING: '#38BDF8',
  RUNNING: '#38BDF8',
};

export function colorForStatus(status) {
  return STATUS_COLORS[status] || colorFor(status || 'UNKNOWN');
}

const PRIORITY_STYLE = {
  HIGH: { color: '#F43F5E', bg: 'rgba(244,63,94,0.1)' },
  MEDIUM: { color: '#F59E0B', bg: 'rgba(245,158,11,0.12)' },
  LOW: { color: '#22C55E', bg: 'rgba(34,197,94,0.1)' },
};

export function priorityStyle(priority) {
  return PRIORITY_STYLE[priority] || { color: 'var(--text-muted)', bg: 'var(--bg-light)' };
}

const ACTIVITY_ICON = {
  ACCEPTED: '✓',
  WRONG_ANSWER: '✕',
  TIME_LIMIT_EXCEEDED: '⏱',
  MEMORY_LIMIT_EXCEEDED: '⏱',
  RUNTIME_ERROR: '!',
  COMPILATION_ERROR: '!',
};

export function iconForActivity(type) {
  return ACTIVITY_ICON[type] || '•';
}

export function relativeTime(isoString) {
  if (!isoString) return '';
  const then = new Date(isoString).getTime();
  if (Number.isNaN(then)) return '';
  const diffSec = Math.max(0, Math.floor((Date.now() - then) / 1000));

  if (diffSec < 60) return 'just now';
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr}h ago`;
  const diffDay = Math.floor(diffHr / 24);
  if (diffDay < 30) return `${diffDay}d ago`;
  return new Date(isoString).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}
