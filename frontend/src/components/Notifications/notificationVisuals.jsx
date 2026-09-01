export const SOURCE_THEME = {
  LEETCODE: {
    label: 'LeetCode',
    accent: '#FBBF24',
    light: 'rgba(251, 191, 36, 0.16)',
    border: 'rgba(251, 191, 36, 0.3)',
    banner: 'linear-gradient(180deg, #FBBF24 0%, #D97706 100%)',
    ring: 'rgba(251, 191, 36, 0.45)',
    Icon: CodeIcon,
  },
  CODEFORCES: {
    label: 'Codeforces',
    accent: '#60A5FA',
    light: 'rgba(96, 165, 250, 0.16)',
    border: 'rgba(96, 165, 250, 0.3)',
    banner: 'linear-gradient(180deg, #60A5FA 0%, #2563EB 100%)',
    ring: 'rgba(96, 165, 250, 0.45)',
    Icon: TrophyIcon,
  },
  SYSTEM: {
    label: 'System',
    accent: '#34D399',
    light: 'rgba(52, 211, 153, 0.16)',
    border: 'rgba(52, 211, 153, 0.3)',
    banner: 'linear-gradient(180deg, #34D399 0%, #059669 100%)',
    ring: 'rgba(52, 211, 153, 0.45)',
    Icon: BellRingIcon,
  },
};

export function themeFor(source) {
  return SOURCE_THEME[source] ?? SOURCE_THEME.SYSTEM;
}

export function CodeIcon({ size = 15 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 6 3 12l6 6M15 6l6 6-6 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function TrophyIcon({ size = 15 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M8 4h8v4a4 4 0 0 1-8 0V4Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <path d="M8 5H5a1 1 0 0 0-1 1c0 2.5 1.8 4.3 4 4.6M16 5h3a1 1 0 0 1 1 1c0 2.5-1.8 4.3-4 4.6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      <path d="M12 12v3M9 19h6M9.5 19c0-2 .8-3 2.5-3s2.5 1 2.5 3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function BellRingIcon({ size = 15 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M18 8a6 6 0 1 0-12 0c0 5-2 6-2 6h16s-2-1-2-6Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <path d="M10 20a2 2 0 0 0 4 0" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
    </svg>
  );
}

export function HourglassIcon({ size = 13 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M6 2h12M6 22h12M7 2c0 5 3.5 6.5 3.5 10S7 17 7 22M17 2c0 5-3.5 6.5-3.5 10S17 17 17 22"
        stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"
      />
    </svg>
  );
}

export function CheckIcon({ size = 14 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 12.5 9.5 18 20 6" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function DoubleCheckIcon({ size = 15 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M2 12.5 6.5 17 15 7" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M9.5 12.5 14 17 22 7" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function XIcon({ size = 14 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M5 5l14 14M19 5 5 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

export function ExternalLinkIcon({ size = 12 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M14 4h6v6M20 4 10 14M9 5H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function EmptyBellIcon({ size = 72 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 96 96" fill="none" aria-hidden="true">
      <circle cx="48" cy="48" r="46" fill="url(#notif-empty-grad)" opacity="0.5" />
      <defs>
        <linearGradient id="notif-empty-grad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#312454" />
          <stop offset="100%" stopColor="#241D3F" />
        </linearGradient>
      </defs>
      <path
        d="M62 54c0-14-5-19-5-19a9 9 0 0 0-18 0s-5 5-5 19h28Z"
        stroke="#A5B4FC" strokeWidth="2.4" strokeLinejoin="round" fill="rgba(99,102,241,0.12)"
      />
      <path d="M41 60a7 7 0 0 0 14 0" stroke="#A5B4FC" strokeWidth="2.4" strokeLinecap="round" />
      <path d="M24 30 30 34M72 30 66 34" stroke="#C4B5FD" strokeWidth="2.2" strokeLinecap="round" />
    </svg>
  );
}

export function parseContestStart(message) {
  const match = /Starts at:\s*(.+)/i.exec(message || '');
  if (!match) return null;
  const raw = match[1].trim();

  if (/^\d{9,}$/.test(raw)) {
    return new Date(Number(raw) * 1000);
  }
  const hasZone = /Z$|[+-]\d{2}:\d{2}$/.test(raw);
  return new Date(hasZone ? raw : `${raw}+06:00`);
}

export function linkFor(n) {
  if (n.source === 'LEETCODE' && n.externalId) {
    const slug = n.externalId.replace(/^LC_/, '');
    return { href: `https://leetcode.com/contest/${slug}/`, external: true, label: 'View on LeetCode' };
  }
  if (n.source === 'CODEFORCES' && n.externalId) {
    const id = n.externalId.replace(/^CF_/, '');
    return { href: `https://codeforces.com/contest/${id}`, external: true, label: 'View on Codeforces' };
  }
  if (n.source === 'SYSTEM' && n.externalId?.startsWith('EXAM_')) {
    const examId = n.externalId.replace(/^EXAM_/, '');
    return { href: `/contests/${examId}`, external: false, label: 'View exam' };
  }
  return null;
}

export function formatRelativeTime(iso) {
  const date = new Date(iso);
  const diffMs = Date.now() - date.getTime();
  const sec = Math.floor(diffMs / 1000);
  if (sec < 5) return 'just now';
  if (sec < 60) return `${sec}s ago`;
  const min = Math.floor(sec / 60);
  if (min < 60) return `${min}m ago`;
  const hr = Math.floor(min / 60);
  if (hr < 24) return `${hr}h ago`;
  const day = Math.floor(hr / 24);
  if (day < 7) return `${day}d ago`;
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

export function getTimeSection(iso) {
  const d = new Date(iso);
  const now = new Date();
  const startOfDay = (x) => new Date(x.getFullYear(), x.getMonth(), x.getDate()).getTime();
  const diffDays = Math.round((startOfDay(now) - startOfDay(d)) / 86400000);
  
  if (diffDays <= 7) return 'THIS_WEEK';
  if (diffDays <= 30) return 'EARLIER';
  return 'PAST';
}