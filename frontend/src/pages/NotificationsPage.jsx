import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getUserIdFromToken, logout } from '../services/api';
import {
  fetchNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  hideNotification,
} from '../services/notificationService';
import { useCountdown, formatDuration } from '../components/Contests/useCountdown';
import {
  themeFor,
  parseContestStart,
  linkFor,
  formatRelativeTime,
  HourglassIcon,
  CheckIcon,
  DoubleCheckIcon,
  XIcon,
  ExternalLinkIcon,
  EmptyBellIcon,
  BellRingIcon,
} from '../components/Notifications/notificationVisuals';
import './Notifications.css'; // Adjust the path to your CSS file as needed

const TIME_TABS = [
  { key: 'THIS_WEEK', label: 'This week' },
  { key: 'EARLIER', label: 'Earlier' },
  { key: 'PAST', label: 'Past' },
];

export default function NotificationsPage() {
  const navigate = useNavigate();
  const userId = getUserIdFromToken();

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('THIS_WEEK');
  const [leavingIds, setLeavingIds] = useState(() => new Set());
  const [markingAll, setMarkingAll] = useState(false);

  const load = useCallback(async () => {
    if (!userId) {
      setLoading(false);
      setError('You need to be logged in to see your notifications.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const data = await fetchNotifications(userId);
      const list = Array.isArray(data) ? data : data?.data || [];
      list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setNotifications(list);
    } catch (e) {
      setError(e.message || 'Could not load notifications.');
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => { load(); }, [load]);

  const visible = useMemo(() => notifications.filter((n) => !n.hidden), [notifications]);

  const counts = useMemo(() => {
    const c = { THIS_WEEK: 0, EARLIER: 0, PAST: 0, UNREAD: 0 };
    for (const n of visible) {
      if (!n.readStatus) c.UNREAD += 1;
      const section = getTimeSection(n.createdAt);
      if (c[section] !== undefined) c[section] += 1;
    }
    return c;
  }, [visible]);

  function getTimeSection(iso) {
    const d = new Date(iso);
    const now = new Date();
    const startOfDay = (x) => new Date(x.getFullYear(), x.getMonth(), x.getDate()).getTime();
    const diffDays = Math.round((startOfDay(now) - startOfDay(d)) / 86400000);
    if (diffDays <= 7) return 'THIS_WEEK';
    if (diffDays <= 30) return 'EARLIER';
    return 'PAST';
  }

  const filtered = useMemo(() => {
    return visible.filter((n) => getTimeSection(n.createdAt) === filter);
  }, [visible, filter]);

  function patch(id, changes) {
    setNotifications((prev) => prev.map((n) => (n.notificationId === id ? { ...n, ...changes } : n)));
  }

  function handleMarkAsRead(n) {
    if (n.readStatus) return;
    patch(n.notificationId, { readStatus: true, readAt: new Date().toISOString() });
    markNotificationAsRead(userId, n.notificationId).catch((e) =>
      console.error('markNotificationAsRead failed:', e.message)
    );
  }

  async function handleMarkAllAsRead() {
    if (counts.UNREAD === 0) return;
    setMarkingAll(true);
    setNotifications((prev) => prev.map((n) => (n.readStatus ? n : { ...n, readStatus: true, readAt: new Date().toISOString() })));
    try {
      await markAllNotificationsAsRead(userId);
    } catch (e) {
      console.error('markAllNotificationsAsRead failed:', e.message);
    } finally {
      setMarkingAll(false);
    }
  }

  function handleDismiss(n) {
    setLeavingIds((prev) => new Set(prev).add(n.notificationId));
    setTimeout(() => {
      setNotifications((prev) => prev.map((x) => (x.notificationId === n.notificationId ? { ...x, hidden: true } : x)));
      setLeavingIds((prev) => {
        const next = new Set(prev);
        next.delete(n.notificationId);
        return next;
      });
    }, 320);
    hideNotification(userId, n.notificationId).catch((e) =>
      console.error('hideNotification failed:', e.message)
    );
  }

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="notif-root">
      <header className="notif-nav">
        <div className="notif-nav-inner">
          <Link to="/" className="notif-logo">
            <span className="notif-logo-icon">{'</>'}</span>
            <span>Nextcoder</span>
          </Link>
          <nav className="notif-nav-links">
            <Link to="/problems">Problems</Link>
            <Link to="/contests">Contests</Link>
            <Link to="/notifications" className="active">Notifications</Link>
          </nav>
          <div className="notif-nav-right">
            <button className="notif-btn-logout" onClick={handleLogout}>Log Out</button>
          </div>
        </div>
      </header>

      <div className="notif-page">
        <div className="notif-hero">
          <div className={`notif-hero-icon ${counts.UNREAD > 0 ? 'is-ringing' : ''}`}>
            <BellRingIcon size={26} />
            {counts.UNREAD > 0 && <span className="notif-hero-badge">{counts.UNREAD > 99 ? '99+' : counts.UNREAD}</span>}
          </div>
          <div className="notif-hero-copy">
            <span className="notif-eyebrow">Notification Center</span>
            <h1>Stay on top of every contest</h1>
            <p>Your LeetCode, Codeforces, and system alerts — unified, filtered, and ready to act on.</p>
          </div>
          <button
            className="notif-markall"
            onClick={handleMarkAllAsRead}
            disabled={counts.UNREAD === 0 || markingAll}
          >
            {markingAll ? <span className="notif-spinner" /> : <DoubleCheckIcon />}
            Mark all as read
          </button>
        </div>

        <div className="notif-time-switcher" role="tablist">
          {TIME_TABS.map((tab) => (
            <button
              key={tab.key}
              className={`notif-time-tab ${filter === tab.key ? 'active' : ''}`}
              onClick={() => setFilter(tab.key)}
              role="tab"
              aria-selected={filter === tab.key}
            >
              {tab.label}
              <span className="notif-tab-count">{counts[tab.key] ?? 0}</span>
            </button>
          ))}
        </div>

        <div className="notif-list">
          {loading && Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} index={i} />)}

          {!loading && error && (
            <div className="notif-error">
              <p>{error}</p>
              {userId && <button className="btn-outline btn-sm" onClick={load}>Try again</button>}
            </div>
          )}

          {!loading && !error && filtered.length === 0 && (
            <div className="notif-empty">
              <EmptyBellIcon />
              <h3>Nothing here yet</h3>
              <p>No notifications match this timeframe.</p>
            </div>
          )}

          {!loading && !error && filtered.map((n, i) => (
            <NotificationCard
              key={n.notificationId}
              n={n}
              index={i}
              leaving={leavingIds.has(n.notificationId)}
              onMarkAsRead={() => handleMarkAsRead(n)}
              onDismiss={() => handleDismiss(n)}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

function NotificationCard({ n, index, leaving, onMarkAsRead, onDismiss }) {
  const theme = themeFor(n.source);
  const { Icon } = theme;
  const isContest = n.type === 'CONTEST';
  const startDate = isContest ? parseContestStart(n.message) : null;
  const target = startDate ? startDate.getTime() : null;
  const diff = useCountdown(target ?? Infinity);
  const link = linkFor(n);

  let countdownLabel = null;
  if (target) {
    countdownLabel = diff > 0 ? `Starts in ${formatDuration(diff)}` : 'In progress / ended';
  }

  const plainMessage = isContest ? null : n.message;
  const startLabel = startDate
    ? startDate.toLocaleString(undefined, {
        weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
      })
    : null;

  function openLink(e) {
    e.stopPropagation();
    onMarkAsRead();
  }

  return (
    <article
      className={`notif-card ${n.readStatus ? 'is-read' : 'is-unread'} ${leaving ? 'is-leaving' : ''}`}
      style={{ '--i': index, '--accent': theme.accent, '--accent-light': theme.light, '--accent-border': theme.border }}
      onClick={onMarkAsRead}
    >
      <span className="notif-card-rail" style={{ background: theme.banner }} />

      {!n.readStatus && <span className="notif-dot" aria-hidden="true" />}

      <div className="notif-card-icon" style={{ background: theme.light, color: theme.accent }}>
        <Icon size={17} />
      </div>

      <div className="notif-card-body">
        <div className="notif-card-top">
          <span className="notif-badge" style={{ background: theme.light, color: theme.accent }}>
            {theme.label}
          </span>
          <span className="notif-time">{formatRelativeTime(n.createdAt)}</span>
        </div>

        <h3 className="notif-card-title">{n.title}</h3>

        {plainMessage && <p className="notif-card-message">{plainMessage}</p>}

        {isContest && (
          <div className="notif-card-contest">
            {startLabel && <span className="notif-contest-date">{startLabel}</span>}
            {countdownLabel && (
              <span className="notif-countdown">
                <HourglassIcon />
                {countdownLabel}
              </span>
            )}
          </div>
        )}

        {link && (
          link.external ? (
            <a className="notif-link" href={link.href} target="_blank" rel="noreferrer" onClick={openLink}>
              {link.label} <ExternalLinkIcon />
            </a>
          ) : (
            <Link className="notif-link" to={link.href} onClick={openLink}>
              {link.label} <ExternalLinkIcon />
            </Link>
          )
        )}
      </div>

      <div className="notif-card-actions">
        {!n.readStatus && (
          <button
            type="button"
            className="notif-icon-btn"
            title="Mark as read"
            aria-label="Mark as read"
            onClick={(e) => { e.stopPropagation(); onMarkAsRead(); }}
          >
            <CheckIcon />
          </button>
        )}
        <button
          type="button"
          className="notif-icon-btn notif-icon-btn--danger"
          title="Dismiss"
          aria-label="Dismiss notification"
          onClick={(e) => { e.stopPropagation(); onDismiss(); }}
        >
          <XIcon />
        </button>
      </div>
    </article>
  );
}

function SkeletonCard({ index }) {
  return (
    <div className="notif-skel" style={{ '--i': index }}>
      <div className="notif-skel-icon" />
      <div className="notif-skel-lines">
        <div className="notif-skel-line short" />
        <div className="notif-skel-line" />
        <div className="notif-skel-line medium" />
      </div>
    </div>
  );
}