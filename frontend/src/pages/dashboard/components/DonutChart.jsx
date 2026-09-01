/**
 * Dependency-free SVG donut chart.
 * segments: [{ label, value, color }]
 */
export default function DonutChart({
  segments,
  size = 168,
  strokeWidth = 20,
  centerValue,
  centerLabel,
}) {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const total = segments.reduce((sum, s) => sum + s.value, 0);

  let offsetAccum = 0;

  return (
    <div className="donut-wrap" style={{ width: size, height: size }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="donut-svg">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="var(--border)"
          strokeWidth={strokeWidth}
        />
        {total > 0 &&
          segments
            .filter((s) => s.value > 0)
            .map((s, i) => {
              const fraction = s.value / total;
              const dash = fraction * circumference;
              const gap = circumference - dash;
              const dashoffset = -offsetAccum * circumference;
              offsetAccum += fraction;
              return (
                <circle
                  key={i}
                  cx={size / 2}
                  cy={size / 2}
                  r={radius}
                  fill="none"
                  stroke={s.color}
                  strokeWidth={strokeWidth}
                  strokeDasharray={`${dash} ${gap}`}
                  strokeDashoffset={dashoffset}
                  strokeLinecap={segments.filter((x) => x.value > 0).length > 1 ? 'butt' : 'round'}
                  transform={`rotate(-90 ${size / 2} ${size / 2})`}
                  className="donut-seg"
                >
                  <title>{`${s.label}: ${s.value}`}</title>
                </circle>
              );
            })}
      </svg>
      <div className="donut-center">
        <span className="donut-center-value">{centerValue}</span>
        {centerLabel && <span className="donut-center-label">{centerLabel}</span>}
      </div>
    </div>
  );
}
