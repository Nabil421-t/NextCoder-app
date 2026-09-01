import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getDashboard, getUserIdFromToken,logout} from '../../services/api.js';
import './Problems.css';

const TOPICS = [
  { key: 'ALL',               label: 'All Topics',          icon: '⊞' },
  { key: 'ARRAY',             label: 'Array',               icon: '▦',  sub: 'Indexing, sorting, searching' },
  { key: 'STRING',            label: 'String',              icon: 'Tt', sub: 'Manipulation, parsing, pattern matching' },
  { key: 'SLIDING_WINDOW',    label: 'Sliding Window',      icon: '□',  sub: 'Subarray and substring optimization' },
  { key: 'TWO_POINTER',       label: 'Two Pointer',         icon: '→',  sub: 'Paired traversal, in-place solutions' },
  { key: 'RECURSION',         label: 'Recursion',           icon: '↺',  sub: 'Self-referential problem solving' },
  { key: 'BACKTRACKING',      label: 'Backtracking',        icon: '⎇',  sub: 'State-space exploration, pruning' },
  { key: 'DYNAMIC_PROGRAMMING', label: 'Dynamic Programming', icon: '⊕', sub: 'Optimal substructure, memoization' },
  { key: 'GRAPH',             label: 'Graph',               icon: '⬡',  sub: 'BFS, DFS, shortest paths' },
  { key: 'SEGMENT_TREE',      label: 'Segment Tree',        icon: '⊓',  sub: 'Range queries, point updates' },
  { key: 'PREFIX_SUM',        label: 'Prefix Sum',          icon: 'Σ',  sub: 'Cumulative sums, range totals' },
];

const DIFF_COLOR = {
  EASY:   { color: '#27C93F', label: 'Easy' },
  MEDIUM: { color: '#FFBD2E', label: 'Medium' },
  HARD:   { color: '#FF5F56', label: 'Hard' },
};

const PLATFORM_LABEL = {
  LEETCODE:   'LeetCode',
  CODEFORCES: 'CodeForces',
  CUSTOM:     'Custom',
};

export default function ProblemsPage() {
  const navigate = useNavigate();
  const userId = getUserIdFromToken();

  const [problems, setProblems]       = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState('');
  const [activeTopic, setActiveTopic] = useState('ALL');
  const [page, setPage]               = useState(0);
  const [totalPages, setTotalPages]   = useState(1);
  const [search, setSearch]           = useState('');
  const [diffFilter, setDiffFilter]   = useState('ALL');
  const [showTopics, setShowTopics]   = useState(false);

  const fetchProblems = useCallback(async () => {
    if (!userId) { console.log('No userId found in localStorage') }
    setLoading(true);
    setError('');
    try {
      console.log('Fetching problems for userId:', userId);
      const res = await getDashboard(userId, page, 20);
      const paged = res.data; // PagedResponse<ProblemDashboardResponse>
      setProblems(paged.content || []);
      setTotalPages(paged.totalPages || 1);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [userId, page, navigate]);

  useEffect(() => { fetchProblems(); }, [fetchProblems]);

  const filtered = problems.filter(p => {
    const topicMatch = activeTopic === 'ALL' || p.type === activeTopic || p.patternName === activeTopic;
    const diffMatch  = diffFilter  === 'ALL' || p.difficultyLevel === diffFilter;
    const searchMatch = !search || p.title.toLowerCase().includes(search.toLowerCase());
    return topicMatch && diffMatch && searchMatch;
  });

  function handleLogout() { logout(); navigate('/login'); }

  return (
    <div className="prob-root">
      {/* ── Navbar ── */}
      <header className="prob-nav">
        <div className="prob-nav-inner">
          <Link to="/" className="prob-logo">
            <span className="logo-icon-p">{'</>'}</span>
            <span>Nextcoder</span>
          </Link>
          <nav className="prob-nav-links">
            <Link to="/problems" className="active">Problems</Link>
            <a href="#">Contests</a>
            <a href="#">Leaderboard</a>
          </nav>
          <div className="prob-nav-right">
            <button className="btn-logout" onClick={handleLogout}>Log Out</button>
          </div>
        </div>
      </header>

      <div className="prob-body">
        {/* ── Sidebar: 10 topics ── */}
        <aside className={`prob-sidebar ${showTopics ? 'open' : ''}`}>
          <div className="sidebar-title">Topic Library</div>
          {TOPICS.map(t => (
            <button
              key={t.key}
              className={`topic-btn ${activeTopic === t.key ? 'active' : ''}`}
              onClick={() => { setActiveTopic(t.key); setPage(0); setShowTopics(false); }}
            >
              <span className="topic-icon">{t.icon}</span>
              <span className="topic-label">{t.label}</span>
              {activeTopic === t.key && <span className="topic-active-dot" />}
            </button>
          ))}
        </aside>

        {/* ── Main content ── */}
        <main className="prob-main">
          {/* Header row */}
          <div className="prob-header">
            <div>
              <div className="eyebrow-p">PROBLEM SET VIEW</div>
              <h1 className="prob-title">Browse Problems by Topic</h1>
              <p className="prob-subtitle">
                Select any topic to filter the problem list. Each row shows difficulty,
                acceptance rate, and tags — just like LeetCode or CodeForces.
              </p>
            </div>
            <button className="mobile-topic-btn" onClick={() => setShowTopics(s => !s)}>
              ☰ Topics
            </button>
          </div>

          {/* Filters */}
          <div className="prob-filters">
            <input
              className="prob-search"
              placeholder="🔍  Search problems…"
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
            <div className="diff-filters">
              {['ALL','EASY','MEDIUM','HARD'].map(d => (
                <button
                  key={d}
                  className={`diff-btn ${diffFilter === d ? 'active' : ''}`}
                  style={diffFilter === d && d !== 'ALL' ? { borderColor: DIFF_COLOR[d]?.color, color: DIFF_COLOR[d]?.color } : {}}
                  onClick={() => setDiffFilter(d)}
                >
                  {d === 'ALL' ? 'All' : DIFF_COLOR[d].label}
                </button>
              ))}
            </div>
          </div>

          {/* 10-topic overview cards (shown only on ALL) */}
          {activeTopic === 'ALL' && !search && (
            <div className="topic-cards-grid">
              {TOPICS.slice(1).map(t => (
                <button key={t.key} className="topic-card-mini" onClick={() => setActiveTopic(t.key)}>
                  <span className="tc-icon">{t.icon}</span>
                  <div>
                    <div className="tc-name">{t.label}</div>
                    <div className="tc-sub">{t.sub}</div>
                  </div>
                </button>
              ))}
            </div>
          )}

          {/* Table */}
          {loading ? (
            <div className="prob-loading">
              <div className="spinner" />
              <span>Loading problems…</span>
            </div>
          ) : error ? (
            <div className="prob-error">
              <span>⚠ {error}</span>
              <button onClick={fetchProblems}>Retry</button>
            </div>
          ) : filtered.length === 0 ? (
            <div className="prob-empty">No problems found. Try a different filter.</div>
          ) : (
            <div className="prob-table-wrap">
              <table className="prob-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Problem Name</th>
                    <th>Topic</th>
                    <th>Platform</th>
                    <th>Difficulty</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((p, idx) => {
                    const diff = DIFF_COLOR[p.difficultyLevel] || { color: '#9CA3AF', label: p.difficultyLevel };
                    return (
                      <tr
                        key={p.problemId}
                        className={`prob-row ${p.solved ? 'solved' : ''}`}
                        onClick={() => navigate(`/problems/${p.problemId}`)}
                      >
                        <td className="col-num">{page * 20 + idx + 1}</td>
                        <td className="col-name">
                          <span className="prob-name-link">{p.title}</span>
                          {p.patternName && <span className="prob-pattern">{p.patternName}</span>}
                        </td>
                        <td className="col-topic">
                          <span className="tag">{p.type?.replace('_', ' ')}</span>
                        </td>
                        <td className="col-platform">
                          {PLATFORM_LABEL[p.platform] || p.platform || '—'}
                        </td>
                        <td className="col-diff">
                          <span className="diff-dot" style={{ background: diff.color }} />
                          <span style={{ color: diff.color }}>{diff.label}</span>
                        </td>
                        <td className="col-status">
                          {p.solved === true  && <span className="status solved-badge">✓ Solved</span>}
                          {p.solved === false && p.totalAttempts > 0 && <span className="status attempted-badge">~ Attempted</span>}
                          {(p.solved === false && !p.totalAttempts) && <span className="status todo-badge">— Todo</span>}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Prev</button>
              <span>Page {page + 1} of {totalPages}</span>
              <button disabled={page + 1 >= totalPages} onClick={() => setPage(p => p + 1)}>Next →</button>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
