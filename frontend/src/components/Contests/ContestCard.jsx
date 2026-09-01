import { useState } from 'react';
import { useCountdown, formatDuration, getContestStatus, parseServerDate } from './useCountdown';
import { GlassCube, HourglassIcon, AlarmIcon, themeForId } from './ContestVisuals';

const STATUS_META = {
  live: { label: 'Live now' },
  upcoming: { label: 'Upcoming' },
  ended: { label: 'Ended' },
};

export default function ContestCard({ exam, onOpen }) {
  const status = getContestStatus(exam);
  const meta = STATUS_META[status] ?? STATUS_META.ended;
  const theme = themeForId(exam.examId);
  const [reminder, setReminder] = useState(false);

  const start = parseServerDate(exam.startTime).getTime();
  const end = start + (exam.duration || 0) * 60000;
  const target = status === 'upcoming' ? start : end;
  const diff = useCountdown(target);

  const countdownLabel =
    status === 'upcoming'
      ? `Starts in ${formatDuration(diff)}`
      : status === 'live'
      ? `Ends in ${formatDuration(diff)}`
      : null;

  const dateLabel = parseServerDate(exam.startTime).toLocaleString(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <article
      className={`nc-card nc-card--${status}`}
      style={{ '--nc-card-banner': theme.banner, '--nc-card-glow': theme.ring }}
      onClick={() => onOpen(exam.examId)}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && onOpen(exam.examId)}
    >
      <div className="nc-card__banner">
        {countdownLabel ? (
          <span className="nc-card__countdown">
            <HourglassIcon />
            {countdownLabel}
          </span>
        ) : (
          <span className="nc-card__status">
            <span className="nc-card__status-dot" />
            {meta.label}
          </span>
        )}

        <button
          type="button"
          className={`nc-card__bell ${reminder ? 'is-active' : ''}`}
          aria-label={reminder ? 'Remove reminder' : 'Remind me'}
          onClick={(e) => {
            e.stopPropagation();
            setReminder((r) => !r);
          }}
        >
          <AlarmIcon />
        </button>

        <GlassCube from={theme.cubeFrom} to={theme.cubeTo} />
      </div>

      <div className="nc-card__info">
        <h3 className="nc-card__title">{exam.title}</h3>
        <p className="nc-card__desc">{exam.description}</p>

        <div className="nc-card__footer">
          <div>
            <div className="nc-card__date">{dateLabel}</div>
            <div className="nc-card__meta">
              {exam.duration} min
              {exam.problems?.length ? ` \u00b7 ${exam.problems.length} problems` : ''}
            </div>
          </div>
          <div className="nc-card__score">{exam.totalScore} pts</div>
        </div>
      </div>
    </article>
  );
}
