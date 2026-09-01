import './WhyNextcoder.css';

const features = [
  {
    icon: '📖',
    title: 'Topic-Wise Learning',
    desc: 'Structured problem sets organized by topic — Arrays, DP, Graphs, and more. Learn at your own pace.',
  },
  {
    icon: '🏆',
    title: 'Timed Contests',
    desc: 'Compete in real-time coding exams with a 2-hour countdown. Test your skills under pressure.',
  },
  {
    icon: '⚡',
    title: 'Instant Feedback',
    desc: 'Run and submit your code. Get immediate results — Accepted, Wrong Answer, or Rejected — with detailed scores.',
  },
  {
    icon: '🔔',
    title: 'Smart Notifications',
    desc: 'Never miss a contest or new problem. Stay updated with personalized alerts delivered to you.',
  },
];

export default function WhyNextcoder() {
  return (
    <section className="section why-nextcoder">
      <div className="container">
        <div className="eyebrow">WHY NEXTCODER</div>
        <h2 className="section-title">Everything You Need to Become a Better Coder</h2>

        <div className="features-grid">
          {features.map((f, i) => (
            <div key={i} className="feature-card">
              <div className="feature-icon">{f.icon}</div>
              <h3 className="feature-title">{f.title}</h3>
              <p className="feature-desc">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
