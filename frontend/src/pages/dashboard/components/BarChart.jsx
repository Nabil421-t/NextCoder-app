/**
 * Dependency-free bar chart for monthly progress.
 * data: [{ label, value, isCurrent? }]
 */
export default function BarChart({ data, color = 'var(--primary)', height = 160 }) {
  const max = Math.max(1, ...data.map((d) => d.value));

  return (
    <div className="barchart" style={{ height }}>
      {data.map((d, i) => {
        const pct = (d.value / max) * 100;
        return (
          <div className="barchart-col" key={i}>
            <div className="barchart-track">
              <div
                className={`barchart-bar${d.isCurrent ? ' is-current' : ''}${d.value === 0 ? ' is-zero' : ''}`}
                style={{ height: `${Math.max(pct, d.value > 0 ? 4 : 0)}%`, background: color }}
                title={`${d.label}: ${d.value} solved`}
              >
                {d.value > 0 && <span className="barchart-value">{d.value}</span>}
              </div>
            </div>
            <span className={`barchart-label${d.isCurrent ? ' is-current' : ''}`}>{d.label}</span>
          </div>
        );
      })}
    </div>
  );
}
