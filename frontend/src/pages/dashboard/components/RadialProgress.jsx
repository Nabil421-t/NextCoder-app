/**
 * Dependency-free single-value radial progress ring.
 * Renders one arc (0–100%) with a percentage label in the center —
 * lighter-weight than DonutChart, meant for compact stat cards.
 */
export default function RadialProgress({
  value = 0,
  size = 72,
  strokeWidth = 7,
  color = 'var(--primary)',
  trackColor = 'var(--border)',
  label,
}) {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.max(0, Math.min(100, value));
  const dash = (clamped / 100) * circumference;

  return (
    <div className="radial" style={{ width: size, height: size }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={trackColor}
          strokeWidth={strokeWidth}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={`${dash} ${circumference - dash}`}
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
          className="radial-arc"
        />
      </svg>
      <div className="radial-center">
        {label !== undefined ? label : <span className="radial-pct">{Math.round(clamped)}%</span>}
      </div>
    </div>
  );
}