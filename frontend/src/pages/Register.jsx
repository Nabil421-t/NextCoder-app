import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Auth.css';

function PasswordStrength({ password }) {
  const getStrength = () => {
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    return score;
  };
  const score = getStrength();
  const labels = ['', 'Weak', 'Fair', 'Good', 'Strong'];
  const colors = ['', '#FF5F56', '#FFBD2E', '#5B5BD6', '#27C93F'];
  if (!password) return null;
  return (
    <div className="strength-wrap">
      <div className="strength-bars">
        {[1,2,3,4].map(i => (
          <div key={i} className="strength-bar" style={{ background: i <= score ? colors[score] : '#E5E7EB' }} />
        ))}
      </div>
      <span className="strength-label" style={{ color: colors[score] }}>{labels[score]}</span>
    </div>
  );
}

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullname: '', username: '', email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);
  const [particles, setParticles] = useState([]);
  const [focused, setFocused] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [step, setStep] = useState(0); // track field completion for progress

  useEffect(() => {
    const pts = Array.from({ length: 22 }, (_, i) => ({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      size: Math.random() * 3 + 1,
      speed: Math.random() * 20 + 15,
      delay: Math.random() * 5,
      opacity: Math.random() * 0.5 + 0.1,
    }));
    setParticles(pts);
  }, []);

  useEffect(() => {
    const filled = Object.values(form).filter(v => v.length > 0).length;
    setStep(filled);
  }, [form]);

  const validate = () => {
    const e = {};
    if (form.fullname.trim().length < 2) e.fullname = 'Enter your full name';
    if (form.username.trim().length < 3) e.username = 'Username must be 3+ characters';
    if (!/^[a-z0-9_]+$/.test(form.username)) e.username = 'Only lowercase letters, numbers & underscores';
    if (!form.email.includes('@')) e.email = 'Enter a valid email';
    if (form.password.length < 6) e.password = 'Password must be 6+ characters';
    return e;
  };

  const handleSubmit = (ev) => {
    ev.preventDefault();
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      setSubmitted(true);
      setTimeout(() => navigate('/login'), 1400);
    }, 1800);
  };

  const handleChange = (field) => (ev) => {
    setForm(f => ({ ...f, [field]: ev.target.value }));
    setErrors(e => ({ ...e, [field]: '' }));
  };

  const progress = (step / 4) * 100;

  return (
    <div className="auth-root">
      {/* Animated background */}
      <div className="auth-bg">
        <div className="auth-mesh auth-mesh-reg" />
        {particles.map(p => (
          <div
            key={p.id}
            className="auth-particle"
            style={{
              left: `${p.x}%`,
              top: `${p.y}%`,
              width: p.size,
              height: p.size,
              animationDuration: `${p.speed}s`,
              animationDelay: `${p.delay}s`,
              opacity: p.opacity,
            }}
          />
        ))}
        <div className="auth-orb orb-1 orb-reg1" />
        <div className="auth-orb orb-2 orb-reg2" />
        <div className="auth-orb orb-3" />
      </div>

      {/* Card */}
      <div className={`auth-card auth-card-reg ${submitted ? 'auth-success' : ''}`}>
        {/* Left panel */}
        <div className="auth-panel auth-panel-left auth-panel-left-reg">
          <Link to="/" className="auth-logo">
            <span className="logo-icon-auth">{'</>'}</span>
            <span>Nextcoder</span>
          </Link>
          <div className="panel-content">
            <div className="panel-tagline">Start for free.</div>
            <h2 className="panel-headline">Join 10,000+<br />coders today.</h2>
            <p className="panel-sub">Build real skills. Compete in real contests. Get real results.</p>

            <div className="panel-features">
              {['Topic-wise problem library', 'Timed contest exams', 'Instant code feedback', 'Personal progress dashboard'].map((f, i) => (
                <div key={i} className="pf-item">
                  <span className="pf-check">✓</span>
                  <span>{f}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="panel-circles">
            <div className="pc pc-1" /><div className="pc pc-2" /><div className="pc pc-3" />
          </div>
        </div>

        {/* Right form */}
        <div className="auth-panel auth-panel-right">
          {submitted ? (
            <div className="success-state">
              <div className="success-icon">✓</div>
              <div className="success-text">Account created!</div>
              <div className="success-sub">Redirecting to login…</div>
            </div>
          ) : (
            <>
              <div className="form-header">
                <h1 className="form-title">Create Account</h1>
                <p className="form-hint">Already have an account? <Link to="/login" className="auth-link">Sign in</Link></p>
              </div>

              {/* Progress bar */}
              <div className="progress-wrap">
                <div className="progress-bar" style={{ width: `${progress}%` }} />
              </div>
              <div className="progress-label">{step === 0 ? 'Fill in your details' : step < 4 ? `${4 - step} field${4 - step > 1 ? 's' : ''} remaining` : 'Looking good! 🎉'}</div>

              <form className="auth-form" onSubmit={handleSubmit} noValidate>
                {/* Full Name */}
                <div className={`field-group ${focused === 'fullname' ? 'field-focused' : ''} ${errors.fullname ? 'field-error' : ''} ${form.fullname ? 'field-filled' : ''}`}>
                  <label className="field-label">Full Name</label>
                  <div className="field-wrap">
                    <span className="field-icon">👤</span>
                    <input
                      type="text"
                      className="field-input"
                      placeholder="John Doe"
                      value={form.fullname}
                      onChange={handleChange('fullname')}
                      onFocus={() => setFocused('fullname')}
                      onBlur={() => setFocused('')}
                    />
                    {form.fullname && !errors.fullname && <span className="field-check">✓</span>}
                    <div className="field-line" />
                  </div>
                  {errors.fullname && <span className="field-err-msg">{errors.fullname}</span>}
                </div>

                {/* Username */}
                <div className={`field-group ${focused === 'username' ? 'field-focused' : ''} ${errors.username ? 'field-error' : ''} ${form.username ? 'field-filled' : ''}`}>
                  <label className="field-label">Username</label>
                  <div className="field-wrap">
                    <span className="field-icon">@</span>
                    <input
                      type="text"
                      className="field-input"
                      placeholder="johndoe_42"
                      value={form.username}
                      onChange={handleChange('username')}
                      onFocus={() => setFocused('username')}
                      onBlur={() => setFocused('')}
                    />
                    {form.username.length >= 3 && !errors.username && <span className="field-check">✓</span>}
                    <div className="field-line" />
                  </div>
                  {errors.username && <span className="field-err-msg">{errors.username}</span>}
                </div>

                {/* Email */}
                <div className={`field-group ${focused === 'email' ? 'field-focused' : ''} ${errors.email ? 'field-error' : ''} ${form.email ? 'field-filled' : ''}`}>
                  <label className="field-label">Email address</label>
                  <div className="field-wrap">
                    <span className="field-icon">✉</span>
                    <input
                      type="email"
                      className="field-input"
                      placeholder="you@example.com"
                      value={form.email}
                      onChange={handleChange('email')}
                      onFocus={() => setFocused('email')}
                      onBlur={() => setFocused('')}
                    />
                    {form.email.includes('@') && <span className="field-check">✓</span>}
                    <div className="field-line" />
                  </div>
                  {errors.email && <span className="field-err-msg">{errors.email}</span>}
                </div>

                {/* Password */}
                <div className={`field-group ${focused === 'password' ? 'field-focused' : ''} ${errors.password ? 'field-error' : ''} ${form.password ? 'field-filled' : ''}`}>
                  <label className="field-label">Password</label>
                  <div className="field-wrap">
                    <span className="field-icon">🔒</span>
                    <input
                      type={showPass ? 'text' : 'password'}
                      className="field-input"
                      placeholder="Min. 6 characters"
                      value={form.password}
                      onChange={handleChange('password')}
                      onFocus={() => setFocused('password')}
                      onBlur={() => setFocused('')}
                    />
                    <button type="button" className="toggle-pass" onClick={() => setShowPass(s => !s)}>
                      {showPass ? '🙈' : '👁'}
                    </button>
                    <div className="field-line" />
                  </div>
                  <PasswordStrength password={form.password} />
                  {errors.password && <span className="field-err-msg">{errors.password}</span>}
                </div>

                <button type="submit" className={`auth-submit-btn ${loading ? 'btn-loading' : ''}`} disabled={loading}>
                  {loading ? (
                    <span className="btn-spinner">
                      <span className="spinner-ring" />
                      Creating account…
                    </span>
                  ) : 'Create Account →'}
                </button>

                <p className="terms-note">
                  By signing up you agree to our <a href="#" className="auth-link">Terms</a> and <a href="#" className="auth-link">Privacy Policy</a>.
                </p>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
