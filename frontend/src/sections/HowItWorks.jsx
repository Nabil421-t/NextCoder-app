import './HowItWorks.css';

const steps = [
  { icon: '👤', label: 'Sign Up' },
  { icon: '📚', label: 'Pick Topic' },
  { icon: '</>', label: 'Solve & Submit' },
  { icon: '🏆', label: 'Take Exams' },
];

export default function HowItWorks() {
  return (
    <section className="section how-it-works" id="how-it-works">
      <div className="container">
        <div className="eyebrow">HOW IT WORKS</div>
        <h2 className="section-title">Up and Running in Minutes</h2>

        <div className="steps-row">
          {steps.map((step, i) => (
            <div key={i} className="step-wrapper">
              <div className="step-circle">
                <span className="step-icon">{step.icon}</span>
              </div>
              {i < steps.length - 1 && (
                <div className="step-arrow">▶</div>
              )}
            </div>
          ))}
        </div>

        <div className="step-labels">
          {steps.map((step, i) => (
            <div key={i} className="step-label">{step.label}</div>
          ))}
        </div>

        <p className="how-desc">
          Nextcoder is designed for clarity and speed. Whether you're preparing for interviews
          or sharpening your competitive edge, the path from signup to submission is seamless.
        </p>
      </div>
    </section>
  );
}
