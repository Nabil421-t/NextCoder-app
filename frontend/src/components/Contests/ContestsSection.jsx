import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchAllExams } from '../../services/examService';
import { getContestStatus, parseServerDate } from './useCountdown';
import ContestCard from './ContestCard';
import './contests-theme.css';
import './contests.css';

const TABS = [
  { key: 'live', label: 'Live' },
  { key: 'upcoming', label: 'Upcoming' },
  { key: 'ended', label: 'Ended' },
];

export default function ContestsSection() {
  const navigate = useNavigate();
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tab, setTab] = useState('live');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchAllExams()
      .then((data) => {
        if (!cancelled) setExams(data ?? []);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const grouped = useMemo(() => {
    const groups = { live: [], upcoming: [], ended: [] };
    for (const exam of exams) {
      const status = getContestStatus(exam);
      if (groups[status]) groups[status].push(exam);
    }
    groups.live.sort((a, b) => parseServerDate(a.startTime) - parseServerDate(b.startTime));
    groups.upcoming.sort((a, b) => parseServerDate(a.startTime) - parseServerDate(b.startTime));
    groups.ended.sort((a, b) => parseServerDate(b.startTime) - parseServerDate(a.startTime));
    return groups;
  }, [exams]);

  const visible = grouped[tab] ?? [];

  return (
    <section className="nc-contests">
      <div className="nc-star-field" aria-hidden="true" />
      <div className="nc-contests__glow" aria-hidden="true" />

      <div className="nc-contests__inner">
        <button className="nc-detail__back" onClick={() => navigate('/')}>
          &larr; Back to Home
        </button>

        <header className="nc-contests__header">
          <span className="nc-contests__brand">
            <span className="nc-contests__brand-mark">{'</>'}</span>
            NextCoder Contest
          </span>
          <h2 className="nc-contests__title">Contests</h2>
          <p className="nc-contests__subtitle">
            Timed DSA exams, scored the moment you submit. Jump into what's live, or set your sights
            on what's next.
          </p>
        </header>

        <div className="nc-contests__tabs" role="tablist">
          {TABS.map((t) => (
            <button
              key={t.key}
              role="tab"
              aria-selected={tab === t.key}
              className={`nc-tab ${tab === t.key ? 'is-active' : ''}`}
              onClick={() => setTab(t.key)}
            >
              {t.label}
              <span className="nc-tab__count">{grouped[t.key]?.length ?? 0}</span>
            </button>
          ))}
        </div>

        {loading && <ContestsSkeleton />}

        {!loading && error && (
          <div className="nc-contests__empty">
            <p>Couldn't load contests right now.</p>
            <span>{error}</span>
          </div>
        )}

        {!loading && !error && visible.length === 0 && (
          <div className="nc-contests__empty">
            <p>
              {tab === 'live' && 'No contests are live right now.'}
              {tab === 'upcoming' && 'Nothing scheduled yet — check back soon.'}
              {tab === 'ended' && "You haven't completed any contests yet."}
            </p>
          </div>
        )}

        {!loading && !error && visible.length > 0 && (
          <div className="nc-contests__grid">
            {visible.map((exam) => (
              <ContestCard
                key={exam.examId}
                exam={exam}
                onOpen={(id) => navigate(`/contests/${id}`)}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

function ContestsSkeleton() {
  return (
    <div className="nc-contests__grid">
      {[0, 1, 2].map((i) => (
        <div key={i} className="nc-skeleton-card" />
      ))}
    </div>
  );
}
