import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { loginUser } from '../services/api.js';
//import { handleResponse } from '../services/api.js';

import './Auth.css';

export default function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ identifier: '', password: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);
  const [particles, setParticles] = useState([]);
  const [focused, setFocused] = useState('');
  const [submitted, setSubmitted] = useState(false);

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

  const validate = () => {
    const e = {};
    if (!form.identifier.includes('@')) e.identifier = 'Enter a valid email address or username';
    if (form.password.length < 6) e.password = 'Password must be 6+ characters';
    return e;
  };

  // ✅ All 3 bugs fixed
  const handleSubmit = async (ev) => {    // async
    ev.preventDefault();
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }  // validate FIRST, then return

    setLoading(true);
    try {
      const response = await loginUser({                   // pass form values, not error object
        identifier: form.identifier,
        password: form.password,
      });
      setSubmitted(true);
      //localStorage.setItem('token', response.token || response.accessToken || '');
      console.log('Login successful, token stored:', response?.data?.accessToken);
      setTimeout(() => navigate('/problems'), 1200);
    } catch (err) {
      setErrors({ general: err.message });
    } finally {
      setLoading(false);
    }
  };
  const handleChange = (field) => (ev) => {
    setForm(f => ({ ...f, [field]: ev.target.value }));
    setErrors(e => ({ ...e, [field]: '' }));
  };

  return (
    <div className="auth-root">
      {/* Animated background */}
      <div className="auth-bg">
        <div className="auth-mesh" />
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
        <div className="auth-orb orb-1" />
        <div className="auth-orb orb-2" />
        <div className="auth-orb orb-3" />
      </div>

      {/* Card */}
      <div className={`auth-card ${submitted ? 'auth-success' : ''}`}>
        {/* Left panel */}
        <div className="auth-panel auth-panel-left">
          <Link to="/" className="auth-logo">
            <span className="logo-icon-auth">{'</>'}</span>
            <span>Nextcoder</span>
          </Link>
          <div className="panel-content">
            <div className="panel-tagline">Welcome back, coder.</div>
            <h2 className="panel-headline">Pick up where<br />you left off.</h2>
            <p className="panel-sub">Your problems, contests, and progress are waiting.</p>
            <div className="panel-stats">
              <div className="pstat"><span className="pstat-num">500+</span><span className="pstat-lbl">Problems</span></div>
              <div className="pstat"><span className="pstat-num">24/7</span><span className="pstat-lbl">Available</span></div>
              <div className="pstat"><span className="pstat-num">20+</span><span className="pstat-lbl">Topics</span></div>
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
              <div className="success-text">Welcome back!</div>
            </div>
          ) : (
            <>
              <div className="form-header">
                <h1 className="form-title">Log In</h1>
                <p className="form-hint">Don't have an account? <Link to="/register" className="auth-link">Sign up free</Link></p>
              </div>

              <form className="auth-form" onSubmit={handleSubmit} noValidate>
                {/* Email */}
                <div className={`field-group ${focused === 'identifier' ? 'field-focused' : ''} ${errors.identifier ? 'field-error' : ''} ${form.identifier ? 'field-filled' : ''}`}>
                  <label className="field-label">Email address or username</label>
                  <div className="field-wrap">
                    <span className="field-icon">✉</span>
                    <input
                      type="identifier"
                      className="field-input"
                      placeholder="you@example.com"
                      value={form.identifier}
                      onChange={handleChange('identifier')}
                      onFocus={() => setFocused('identifier')}
                      onBlur={() => setFocused('')}
                    />
                    <div className="field-line" />
                  </div>
                  {errors.identifier && <span className="field-err-msg">{errors.identifier}</span>}
                </div>

                {/* Password */}
                <div className={`field-group ${focused === 'password' ? 'field-focused' : ''} ${errors.password ? 'field-error' : ''} ${form.password ? 'field-filled' : ''}`}>
                  <label className="field-label">Password</label>
                  <div className="field-wrap">
                    <span className="field-icon">🔒</span>
                    <input
                      type={showPass ? 'text' : 'password'}
                      className="field-input"
                      placeholder="••••••••"
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
                  {errors.password && <span className="field-err-msg">{errors.password}</span>}
                </div>

                <div className="form-row-inline">
                  <a href="#" className="forgot-link">Forgot password?</a>
                </div>

                <button type="submit" className={`auth-submit-btn ${loading ? 'btn-loading' : ''}`} disabled={loading}>
                  {loading ? (
                    <span className="btn-spinner">
                      <span className="spinner-ring" />
                      Signing in…
                    </span>
                  ) : 'Sign In →'}
                </button>
              </form>
              <div className="auth-divider"><span>or continue with</span></div>
              <div className="oauth-row">
                <button className="oauth-btn">
                  <svg width="18" height="18" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
                  Google
                </button>
                <button className="oauth-btn">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/></svg>
                  GitHub
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
