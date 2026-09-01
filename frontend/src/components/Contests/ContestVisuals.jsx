/**
 * ContestVisuals.jsx
 *
 * Rotating banner themes + the isometric "glass cube" graphic used on
 * contest cards and the detail hero, so every contest gets its own
 * distinct, vivid identity instead of one flat color.
 */

export const CARD_THEMES = [
  {
    id: 'ember',
    banner: 'linear-gradient(135deg, #fbbf24 0%, #f97316 55%, #ea580c 100%)',
    cubeFrom: '#fff7d6',
    cubeTo: '#f59e0b',
    ring: 'rgba(251, 191, 36, 0.55)',
  },
  {
    id: 'nova',
    banner: 'linear-gradient(135deg, #a78bfa 0%, #7c3aed 55%, #4338ca 100%)',
    cubeFrom: '#ede9fe',
    cubeTo: '#8b5cf6',
    ring: 'rgba(167, 139, 250, 0.55)',
  },
  {
    id: 'reef',
    banner: 'linear-gradient(135deg, #34d399 0%, #10b981 55%, #047857 100%)',
    cubeFrom: '#d1fae5',
    cubeTo: '#10b981',
    ring: 'rgba(52, 211, 153, 0.55)',
  },
  {
    id: 'bloom',
    banner: 'linear-gradient(135deg, #f9a8d4 0%, #ec4899 55%, #be185d 100%)',
    cubeFrom: '#fce7f3',
    cubeTo: '#ec4899',
    ring: 'rgba(244, 114, 182, 0.55)',
  },
  {
    id: 'tide',
    banner: 'linear-gradient(135deg, #67e8f9 0%, #06b6d4 55%, #0e7490 100%)',
    cubeFrom: '#cffafe',
    cubeTo: '#06b6d4',
    ring: 'rgba(103, 232, 249, 0.55)',
  },
];

export function themeForIndex(i = 0) {
  return CARD_THEMES[Math.abs(i) % CARD_THEMES.length];
}

/** Deterministic theme pick from an exam id, so a card keeps its color
 * across re-fetches/re-sorts instead of jumping when the list re-orders. */
export function themeForId(id) {
  const s = String(id ?? '');
  let hash = 0;
  for (let i = 0; i < s.length; i++) hash = (hash * 31 + s.charCodeAt(i)) | 0;
  return themeForIndex(hash);
}

export function GlassCube({ from, to, size = 92 }) {
  const uid = `${from}-${to}`.replace(/[^a-zA-Z0-9]/g, '');
  return (
    <svg
      className="nc-cube"
      width={size}
      height={size}
      viewBox="0 0 100 100"
      aria-hidden="true"
    >
      <defs>
        <linearGradient id={`cubeTop-${uid}`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor={from} stopOpacity="0.95" />
          <stop offset="100%" stopColor={to} stopOpacity="0.65" />
        </linearGradient>
        <linearGradient id={`cubeLeft-${uid}`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor={to} stopOpacity="0.55" />
          <stop offset="100%" stopColor={to} stopOpacity="0.85" />
        </linearGradient>
        <linearGradient id={`cubeRight-${uid}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={from} stopOpacity="0.75" />
          <stop offset="100%" stopColor={to} stopOpacity="0.95" />
        </linearGradient>
      </defs>
      <g>
        <polygon points="50,6 90,26 50,46 10,26" fill={`url(#cubeTop-${uid})`} stroke="rgba(255,255,255,0.5)" strokeWidth="0.75" />
        <polygon points="10,26 50,46 50,94 10,74" fill={`url(#cubeLeft-${uid})`} stroke="rgba(255,255,255,0.25)" strokeWidth="0.75" />
        <polygon points="90,26 50,46 50,94 90,74" fill={`url(#cubeRight-${uid})`} stroke="rgba(255,255,255,0.25)" strokeWidth="0.75" />
        <polygon points="50,6 90,26 50,46 10,26" fill="white" opacity="0.08" />
      </g>
    </svg>
  );
}

export function HourglassIcon() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M6 2h12M6 22h12M7 2c0 5 3.5 6.5 3.5 10S7 17 7 22M17 2c0 5-3.5 6.5-3.5 10S17 17 17 22"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function AlarmIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="13" r="8" stroke="currentColor" strokeWidth="1.7" />
      <path d="M12 9v4l2.5 2.5" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M5 3 2.5 5.5M19 3l2.5 2.5" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
    </svg>
  );
}
