import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchExamDetail, startExam } from '../../services/examService';
import { getContestStatus, parseServerDate } from './useCountdown';
import { GlassCube, themeForId } from './ContestVisuals';
import './contests-theme.css';
import './contests.css';

const DIFFICULTY_CLASS = {
  EASY: 'nc-badge--easy',
  MEDIUM: 'nc-badge--medium',
  HARD: 'nc-badge--hard',
};

export default function ContestDetail() {
  const { examId } = useParams();
  const navigate = useNavigate();
  const [exam, setExam] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [starting, setStarting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchExamDetail(examId)
      .then((data) => !cancelled && setExam(data))
      .catch((err) => !cancelled && setError(err.message))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [examId]);

  async function handleStart() {
    setStarting(true);
    try {
      // startExam is idempotent server-side: safe to call again on
      // refresh, it will just resolve to "resuming" the same session.
      await startExam(examId);
      // Adjust this route to wherever your LobbyView/ExamView flow lives.
      navigate(`/exams/${examId}/session`);
    } catch (err) {
      setError(err.message);
    } finally {
      setStarting(false);
    }
  }

  if (loading) {
    return (
      <section className="nc-detail">
        <div className="nc-star-field" aria-hidden="true" />
        <div className="nc-detail__glow" aria-hidden="true" />
        <div className="nc-detail__inner">
          <div className="nc-detail__skeleton" />
        </div>
      </section>
    );
  }

  if (error || !exam) {
    return (
      <section className="nc-detail">
        <div className="nc-star-field" aria-hidden="true" />
        <div className="nc-detail__glow" aria-hidden="true" />
        <div className="nc-detail__inner">
          <div className="nc-contests__empty">
            <p>Couldn't load this contest.</p>
            {error && <span>{error}</span>}
          </div>
        </div>
      </section>
    );
  }

  const status = getContestStatus(exam);
  const canStart = status === 'live';
  const theme = themeForId(exam.examId);

  return (
    <section className={`nc-detail nc-detail--${status}`}>
      <div className="nc-star-field" aria-hidden="true" />
      <div className="nc-detail__glow" aria-hidden="true" />

      <div className="nc-detail__inner">
        <div style={{ display: 'flex', gap: 20, flexWrap: 'wrap' }}>
          <button className="nc-detail__back" onClick={() => navigate('/contests')}>
            &larr; All contests
          </button>
          <button className="nc-detail__back" onClick={() => navigate('/')}>
            &larr; Back to Home
          </button>
        </div>

        <div className="nc-detail__hero" style={{ '--nc-card-banner': theme.banner }}>
          <div className="nc-detail__hero-text">
            <span className={`nc-status-pill nc-status-pill--${status}`}>
              {status === 'live' ? 'Live now' : status === 'upcoming' ? 'Upcoming' : 'Contest ended'}
            </span>
            <h1 className="nc-detail__title">{exam.title}</h1>
            <p className="nc-detail__desc">{exam.description}</p>
          </div>

          <div className="nc-detail__hero-side">
            <div className="nc-detail__hero-cube">
              <GlassCube from={theme.cubeFrom} to={theme.cubeTo} size={110} />
            </div>
            <button
              className="nc-btn nc-btn--primary"
              disabled={!canStart || starting}
              onClick={handleStart}
              title={!canStart ? 'This contest is not currently live' : undefined}
            >
              {starting ? 'Starting…' : status === 'upcoming' ? 'Not started yet' : status === 'ended' ? 'Contest ended' : 'Start exam'}
            </button>
          </div>
        </div>

        <div className="nc-stat-row">
          <div className="nc-stat">
            <span className="nc-stat__icon">⏱️</span>
            <span className="nc-stat__value">{exam.duration} min</span>
          </div>
          <div className="nc-stat">
            <span className="nc-stat__icon">🏆</span>
            <span className="nc-stat__value">{exam.totalScore} pts</span>
          </div>
          <div className="nc-stat">
            <span className="nc-stat__icon">✅</span>
            <span className="nc-stat__value">Pass: {exam.passingMarks}</span>
          </div>
          <div className="nc-stat">
            <span className="nc-stat__icon">📅</span>
            <span className="nc-stat__value">
              {parseServerDate(exam.startTime).toLocaleString(undefined, {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </span>
          </div>
        </div>

        <div className="nc-problems">
          <h2 className="nc-problems__title">Problems ({exam.problems?.length ?? 0})</h2>
          <div className="nc-problems__list">
            {exam.problems?.map((p, i) => {
              const locked = status === 'upcoming';
              const openProblem = () => {
                if (locked || !p.id) return;
                navigate(`/problems/${p.id}`, { state: { fromExam: exam.examId } });
              };
              return (
                <div
                  className={`nc-problem-row ${locked ? 'nc-problem-row--locked' : ''}`}
                  key={p.id ?? i}
                  role="button"
                  tabIndex={locked ? -1 : 0}
                  aria-disabled={locked}
                  onClick={openProblem}
                  onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && openProblem()}
                >
                  <span className="nc-problem-row__index">{String(i + 1).padStart(2, '0')}</span>
                  <span className="nc-problem-row__title">{p.title}</span>
                  <span className="nc-problem-row__type">
                    {locked ? 'Unlocks when live' : p.type}
                  </span>
                  <span className={`nc-badge ${DIFFICULTY_CLASS[p.difficulty] ?? ''}`}>
                    {p.difficulty}
                  </span>
                  <span className="nc-problem-row__score">{p.score} pts</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}
