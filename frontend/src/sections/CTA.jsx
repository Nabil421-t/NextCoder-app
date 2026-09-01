import { Link } from 'react-router-dom';
import './CTA.css';

export default function CTA() {
  return (
    <section className="section cta-section" id="cta">
      <div className="container">
        <div className="cta-box">
          <h2 className="section-title cta-title">Ready to Level Up?</h2>
          <p className="cta-desc">
            Join thousands of competitive programmers practicing daily. Pick a topic,
            solve a problem, and build the skills that land offers at top tech companies.
          </p>

          <div className="cta-actions">
            <Link to="/register" className="btn-primary cta-btn-primary">Start Your First Problem</Link>
            <Link to="/problems" className="btn-outline cta-btn-outline">View All Topics</Link>
          </div>

          <div className="cta-note">
            <span className="cta-check">☐</span>
            Nextcoder is free to get started. No credit card required.
            Start solving problems in under 2 minutes.
          </div>
        </div>
      </div>
    </section>
  );
}
