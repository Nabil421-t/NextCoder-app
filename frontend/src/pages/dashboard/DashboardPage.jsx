import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getUserIdFromToken } from '../../services/api';
import { getFullDashboard} from '../../services/dashboardService';
import DonutChart from './components/DonutChart';
import RadialProgress from './components/RadialProgress';
import {
  colorFor,
  colorForStatus,
  titleCase,
  priorityStyle,
  iconForActivity,
  relativeTime,
} from './dashboardUtils';
import './Dashboard.css';


function StatCard({ icon, label, value, sub, accent }) {
  return (
    <div className="stat-card">
      <div className="stat-icon" style={{ background: `${accent}1a`, color: accent }}>
        {icon}
      </div>
      <div className="stat-body">
        <span className="stat-value">{value}</span>
        <span className="stat-label">{label}</span>
        {sub && <span className="stat-sub">{sub}</span>}
      </div>
    </div>
  );
}

function EmptyState({ text }) {
  return <div className="dash-empty">{text}</div>;
}

export default function DashboardPage() {
  const userId = getUserIdFromToken();

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeCategory, setActiveCategory] = useState(null);

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      setError('not-authenticated');
      return;
    }
    let cancelled = false;
    setLoading(true);
    getFullDashboard(userId)
      .then((res) => {
        if (cancelled) return;
        setData(res);
        setError(null);
      })
      .catch((err) => !cancelled && setError(err.message || 'Failed to load dashboard'))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [userId]);

  const statusSegments = useMemo(
    () =>
      (data?.statusDistribution || []).map((s) => ({
        label: titleCase(s.status),
        value: s.count,
        color: colorForStatus(s.status),
      })),
    [data]
  );

  const platformSegments = useMemo(
    () =>
      (data?.platformDistribution || []).map((p) => ({
        label: titleCase(p.platform),
        value: p.solvedCount,
        color: colorFor(p.platform),
      })),
    [data]
  );

  if (!userId) {
    return (
      <div className="dashboard-page">
        <div className="container">
          <Link to="/" className="dash-back">&larr; Back to Home</Link>
        </div>
        <div className="dash-auth-gate container">
          <h1>Your dashboard is waiting</h1>
          <p>Log in to see your solved problems, streaks, and progress by category.</p>
          <Link to="/login" className="btn-primary">Log In</Link>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="dashboard-page">
        <div className="container">
          <Link to="/" className="dash-back">&larr; Back to Home</Link>
          <div className="dash-loading">
            <div className="spinner" />
            <span>Loading your dashboard…</span>
          </div>
        </div>
      </div>
    );
  }

  if (error && error !== 'not-authenticated') {
    return (
      <div className="dashboard-page">
        <div className="container">
          <Link to="/" className="dash-back">&larr; Back to Home</Link>
          <div className="dash-error">
            <h2>Couldn't load the dashboard</h2>
            <p>{error}</p>
          </div>
        </div>
      </div>
    );
  }

  const stats = data?.statistics;
  const categories = data?.categoryProgress || [];
  const activity = data?.activity || [];
  const recommendations = data?.recommendations || [];

  return (
    <div className="dashboard-page">
      <div className="dash-header">
        <div className="container">
          <Link to="/" className="dash-back">&larr; Back to Home</Link>
        </div>
        <div className="container dash-header-inner">
          <div>
            <span className="dash-eyebrow">Your Progress</span>
            <h1>Dashboard</h1>
          </div>
          {stats && (
            <div className="dash-streak-chip">
              <span className="flame">🔥</span>
              <div>
                <strong>{stats.currentStreak} day{stats.currentStreak === 1 ? '' : 's'}</strong>
                <span>current streak · best {stats.longestStreak}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="container dash-body">
        {/* ── Stat cards ───────────────────────────── */}
        <div className="stat-grid">
          <StatCard
            icon="✓"
            accent="#22C55E"
            label="Problems Solved"
            value={stats ? `${stats.solvedProblems}` : '—'}
            sub={stats ? `of ${stats.totalProblems} total` : ''}
          />
          <StatCard
            icon="%"
            accent="#5B5BD6"
            label="Acceptance Rate"
            value={stats ? `${stats.acceptanceRate.toFixed(1)}%` : '—'}
            sub={stats ? `${stats.acceptedSubmissions}/${stats.totalSubmissions} submissions` : ''}
          />
          <StatCard
            icon="⚡"
            accent="#F59E0B"
            label="Current Streak"
            value={stats ? stats.currentStreak : '—'}
            sub="days active"
          />
          <StatCard
            icon="🏆"
            accent="#F43F5E"
            label="Longest Streak"
            value={stats ? stats.longestStreak : '—'}
            sub="personal best"
          />
          <StatCard
            icon="#"
            accent="#38BDF8"
            label="Your Rank"
            value={stats ? `#${stats.rank}` : '—'}
            sub="by problems solved"
          />
        </div>

        {/* ── Category progress ─────────────────── */}
        <section className="dash-card cat-section">
          <div className="dash-card-head">
            <h2>Category Progress</h2>
            {categories.length > 0 && (
              <span className="cat-section-sub">{categories.length} categories</span>
            )}
          </div>
          {categories.length === 0 ? (
            <EmptyState text="No category data yet — solve a problem to get started." />
          ) : (
            <div className="category-grid">
              {categories.map((c) => {
                const accent = colorFor(c.category);
                const remaining = Math.max(c.totalProblems - c.solvedProblems, 0);
                const mastered = c.completionPercentage >= 75;
                return (
                  <div
                    className="category-card"
                    key={c.category}
                    style={{ '--cat-accent': accent }}
                  >
                    <RadialProgress
                      value={c.completionPercentage}
                      color={accent}
                      size={76}
                      strokeWidth={7}
                    />
                    <div className="category-card-info">
                      <span className="category-card-name">{titleCase(c.category)}</span>
                      <span className="category-card-count">
                        <strong>{c.solvedProblems}</strong> / {c.totalProblems} solved
                      </span>
                      <span className="category-card-remaining">
                        {mastered ? '🌟 Mastered' : `${remaining} left`}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        <div className="dash-grid">
          {/* ── Status distribution donut ─────────────────── */}
          <section className="dash-card">
            <div className="dash-card-head">
              <h2>Submission Status</h2>
            </div>
            {statusSegments.length === 0 || statusSegments.every((s) => s.value === 0) ? (
              <EmptyState text="No submissions yet." />
            ) : (
              <div className="donut-panel">
                <DonutChart
                  segments={statusSegments}
                  centerValue={stats?.totalSubmissions ?? ''}
                  centerLabel="submissions"
                />
                <ul className="legend">
                  {statusSegments.map((s) => (
                    <li key={s.label}>
                      <span className="legend-dot" style={{ background: s.color }} />
                      {s.label}
                      <span className="legend-value">{s.value}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </section>

          {/* ── Platform distribution donut ─────────────────── */}
          <section className="dash-card">
            <div className="dash-card-head">
              <h2>Platform Split</h2>
            </div>
            {platformSegments.length === 0 || platformSegments.every((p) => p.value === 0) ? (
              <EmptyState text="No solved problems yet." />
            ) : (
              <div className="donut-panel">
                <DonutChart
                  segments={platformSegments}
                  centerValue={stats?.solvedProblems ?? ''}
                  centerLabel="solved"
                />
                <ul className="legend">
                  {platformSegments.map((p) => (
                    <li key={p.label}>
                      <span className="legend-dot" style={{ background: p.color }} />
                      {p.label}
                      <span className="legend-value">{p.value}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </section>

          {/* ── Recent activity ─────────────────── */}
          <section className="dash-card">
            <div className="dash-card-head">
              <h2>Recent Activity</h2>
            </div>
            {activity.length === 0 ? (
              <EmptyState text="No recent activity." />
            ) : (
              <ul className="activity-feed">
                {activity.slice(0, 8).map((a, i) => (
                  <li key={i} className={`activity-item activity-${a.type === 'ACCEPTED' ? 'ok' : 'bad'}`}>
                    <span className="activity-icon">{iconForActivity(a.type)}</span>
                    <div className="activity-text">
                      <span className="activity-desc">{a.description || `${titleCase(a.type)} · ${a.problemTitle}`}</span>
                      <span className="activity-time">{relativeTime(a.occurredAt)}</span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {/* ── Recommendations ─────────────────── */}
          <section className="dash-card">
            <div className="dash-card-head">
              <h2>Recommended Focus</h2>
            </div>
            {recommendations.length === 0 ? (
              <EmptyState text="You're on track everywhere — nice work." />
            ) : (
              <ul className="reco-list">
                {recommendations.map((r) => {
                  const style = priorityStyle(r.priority);
                  return (
                    <li key={r.category} className="reco-item">
                      <div className="reco-top">
                        <span className="reco-name">{titleCase(r.category)}</span>
                        <span className="reco-badge" style={{ color: style.color, background: style.bg }}>
                          {titleCase(r.priority)}
                        </span>
                      </div>
                      <div className="progress-track progress-track--sm">
                        <div
                          className="progress-fill"
                          style={{ width: `${r.completionPercentage}%`, background: style.color }}
                        />
                      </div>
                      <span className="reco-count">
                        {r.solvedProblems}/{r.totalProblems} solved
                      </span>
                    </li>
                  );
                })}
              </ul>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}