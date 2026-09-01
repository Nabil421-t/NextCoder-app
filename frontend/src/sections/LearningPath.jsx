import './LearningPath.css';

const steps = [
  {
    num: 1,
    title: 'Pick a Topic',
    desc: 'Choose from 20+ curated topics like Arrays, Recursion, Trees, and Dynamic Programming.',
  },
  {
    num: 2,
    title: 'Solve Problems',
    desc: 'Work through progressively harder problems with full descriptions and test cases.',
  },
  {
    num: 3,
    title: 'Take an Exam',
    desc: 'Lock in your skills with a timed 3-problem exam. 2 hours on the clock.',
  },
  {
    num: 4,
    title: 'Track Progress',
    desc: 'View your scores, acceptance rates, and rankings on your personal profile.',
  },
];

export default function LearningPath() {
  return (
    <section className="section learning-path" id="learning">
      <div className="container learning-inner">
        <div className="learning-left">
          <div className="eyebrow">LEARNING PATH</div>
          <h2 className="section-title">From Zero to Contest-Ready</h2>
          <p className="section-subtitle">
            Nextcoder's structured learning path takes you from fundamentals to advanced
            competitive programming — one topic at a time.
          </p>

          <div className="learning-steps">
            {steps.map((s) => (
              <div key={s.num} className="lp-step">
                <div className="lp-num">{s.num}</div>
                <div className="lp-content">
                  <h3 className="lp-title">{s.title}</h3>
                  <p className="lp-desc">{s.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="learning-right">
          <div className="learning-illustration">
            <div className="illus-monitor">
              <div className="monitor-screen">
                <div className="monitor-line accent" />
                <div className="monitor-line" />
                <div className="monitor-line short" />
                <div className="monitor-line" />
                <div className="monitor-line accent" />
                <div className="monitor-line short" />
                <div className="monitor-line" />
                <div className="monitor-line" />
              </div>
            </div>
            <div className="illus-badge">💻 Coding in Progress...</div>
          </div>
        </div>
      </div>
    </section>
  );
}
