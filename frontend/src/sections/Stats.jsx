import './Stats.css';

const stats = [
  { num: '500+', label: 'Coding Problems', sub: 'Across 20+ topic categories' },
  { num: '2hrs', label: 'Exam Duration', sub: 'Timed competitive exams' },
  { num: '20+', label: 'Topics Covered', sub: 'From basics to advanced' },
  { num: '24/7', label: 'Always Available', sub: 'Practice anytime, anywhere' },
];

export default function Stats() {
  return (
    <section className="section stats-section">
      <div className="container">
        <div className="eyebrow">PLATFORM STATS</div>
        <h2 className="section-title">Trusted by Developers Worldwide</h2>

        <div className="stats-grid">
          {stats.map((s, i) => (
            <div key={i} className="stat-item">
              <div className="stat-num">{s.num}</div>
              <div className="stat-label">{s.label}</div>
              <div className="stat-sub">{s.sub}</div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
