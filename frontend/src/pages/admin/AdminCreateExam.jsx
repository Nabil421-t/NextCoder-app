import { useState, useEffect, useCallback, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getDashboard, getUserIdFromToken, isAdmin, logout } from '../../services/api';
import { createExam } from '../../services/examService';
import './AdminCreateExam.css';

const DIFF_COLOR = {
  EASY:   { color: '#27C93F', label: 'Easy' },
  MEDIUM: { color: '#FFBD2E', label: 'Medium' },
  HARD:   { color: '#FF5F56', label: 'Hard' },
};

const REQUIRED_PROBLEM_COUNT = 3;

const emptyForm = {
  title: '',
  description: '',
  durationMinutes: 90,
  startTime: '',
  passingMarks: '',
};

export default function AdminCreateExam() {
  const navigate = useNavigate();
  const userId = getUserIdFromToken();
  const admin = isAdmin();

  const [form, setForm] = useState(emptyForm);
  const [selected, setSelected] = useState([]); // [{ problemId, title, difficultyLevel, score }]
  const [problems, setProblems] = useState([]);
  const [loadingProblems, setLoadingProblems] = useState(true);
  const [problemsError, setProblemsError] = useState('');
  const [search, setSearch] = useState('');

  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [created, setCreated] = useState(null);

  const fetchProblems = useCallback(async () => {
    setLoadingProblems(true);
    setProblemsError('');
    try {
      // Reusing the student dashboard endpoint as the problem catalog —
      // swap in a dedicated admin "list all problems" endpoint if you have one.
      const res = await getDashboard(userId, 0, 100);
      setProblems(res.data?.content || []);
    } catch (e) {
      setProblemsError(e.message);
    } finally {
      setLoadingProblems(false);
    }
  }, [userId]);

  useEffect(() => {
    console.log("He is admin",admin);
    if (admin) fetchProblems();
    
  }, [admin, fetchProblems]);

  function handleLogout() { logout(); navigate('/login'); }

  function updateField(key, value) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  function addProblem(p) {
    if (selected.length >= REQUIRED_PROBLEM_COUNT) return;
    if (selected.some((s) => s.problemId === p.problemId)) return;
    setSelected((s) => [...s, { problemId: p.problemId, title: p.title, difficultyLevel: p.difficultyLevel, score: 10 }]);
  }

  function removeProblem(problemId) {
    setSelected((s) => s.filter((p) => p.problemId !== problemId));
  }

  function updateScore(problemId, score) {
    setSelected((s) => s.map((p) => (p.problemId === problemId ? { ...p, score } : p)));
  }

  const filteredProblems = useMemo(() => {
    if (!search.trim()) return problems;
    const q = search.toLowerCase();
    return problems.filter((p) => p.title?.toLowerCase().includes(q));
  }, [problems, search]);

  const totalScore = selected.reduce((sum, p) => sum + (Number(p.score) || 0), 0);

  function validate() {
    if (!form.title.trim()) return 'Exam title is required.';
    if (form.title.length > 200) return 'Title cannot exceed 200 characters.';
    if (!form.description.trim()) return 'Exam description is required.';
    if (!form.durationMinutes || Number(form.durationMinutes) <= 0) return 'Duration must be a positive number of minutes.';
    if (!form.startTime) return 'Start time is required.';
    if (form.passingMarks === '' || Number(form.passingMarks) < 0) return 'Passing marks are required.';
    if (selected.length !== REQUIRED_PROBLEM_COUNT) return `An exam must contain exactly ${REQUIRED_PROBLEM_COUNT} problems (currently ${selected.length}).`;
    if (selected.some((p) => !p.score || Number(p.score) < 1)) return 'Every problem needs a score of at least 1 point.';
    if (Number(form.passingMarks) > totalScore) return `Passing marks (${form.passingMarks}) can't exceed the total score (${totalScore}).`;
    return '';
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const err = validate();
    if (err) { setFormError(err); return; }
    setFormError('');
    setSubmitting(true);
    setCreated(null);
    try {
      const payload = {
        title: form.title.trim(),
        description: form.description.trim(),
        durationMinutes: Number(form.durationMinutes),
        // <input type="datetime-local"> gives "yyyy-MM-ddTHH:mm" — append
        // seconds so it matches LocalDateTime's expected format. This is
        // sent as-is (no timezone math): the value the admin picks IS the
        // server's local wall-clock time (confirmed Asia/Dhaka backend).
        startTime: `${form.startTime}:00`,
        passingMarks: Number(form.passingMarks),
        problems: selected.map((p) => ({ problemId: p.problemId, score: Number(p.score) })),
      };
      const res = await createExam(payload);
      setCreated(res.data);
      setForm(emptyForm);
      setSelected([]);
    } catch (e) {
      setFormError(e.message);
    } finally {
      setSubmitting(false);
    }
  }

  if (!admin) {
    return (
      <div className="prob-root">
        <AdminNavbar onLogout={handleLogout} />
        <div className="ace-denied">
          <h1>Admins only</h1>
          <p>
            You don't have access to this page. If you believe this is a mistake, check that your
            account role is set to ADMIN.
          </p>
          <Link to="/contests" className="ace-btn ace-btn--ghost">Back to Contests</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="prob-root">
      <AdminNavbar onLogout={handleLogout} />

      <div className="ace-body">
        <div className="ace-header">
          <div className="ace-eyebrow">ADMIN</div>
          <h1>Create an exam</h1>
          <p>Set the exam window and pick exactly {REQUIRED_PROBLEM_COUNT} problems for it.</p>
        </div>

        {created && (
          <div className="ace-success">
            <span>✓ "{created.title}" was created.</span>
            <Link to={`/contests/${created.examId}`} className="ace-btn ace-btn--primary">
              View exam
            </Link>
          </div>
        )}

        <form className="ace-grid" onSubmit={handleSubmit}>
          {/* ── Left: exam details ── */}
          <div className="ace-card">
            <h2 className="ace-card-title">Exam details</h2>

            <label className="ace-field">
              <span>Title</span>
              <input
                type="text"
                value={form.title}
                maxLength={200}
                placeholder="e.g. Weekly Contest 6"
                onChange={(e) => updateField('title', e.target.value)}
              />
            </label>

            <label className="ace-field">
              <span>Description</span>
              <textarea
                rows={4}
                value={form.description}
                placeholder="What does this exam cover?"
                onChange={(e) => updateField('description', e.target.value)}
              />
            </label>

            <div className="ace-field-row">
              <label className="ace-field">
                <span>Duration (minutes)</span>
                <input
                  type="number"
                  min={1}
                  value={form.durationMinutes}
                  onChange={(e) => updateField('durationMinutes', e.target.value)}
                />
              </label>

              <label className="ace-field">
                <span>Passing marks</span>
                <input
                  type="number"
                  min={0}
                  value={form.passingMarks}
                  placeholder={totalScore ? `out of ${totalScore}` : '0'}
                  onChange={(e) => updateField('passingMarks', e.target.value)}
                />
              </label>
            </div>

            <label className="ace-field">
              <span>Start time</span>
              <input
                type="datetime-local"
                value={form.startTime}
                onChange={(e) => updateField('startTime', e.target.value)}
              />
            </label>
            <p className="ace-hint">This is the server's local time — enter it as you want it to appear to students.</p>
          </div>

          {/* ── Right: problem picker ── */}
          <div className="ace-card">
            <div className="ace-card-title-row">
              <h2 className="ace-card-title">Problems</h2>
              <span className={`ace-count ${selected.length === REQUIRED_PROBLEM_COUNT ? 'is-complete' : ''}`}>
                {selected.length}/{REQUIRED_PROBLEM_COUNT} selected
              </span>
            </div>

            {selected.length > 0 && (
              <ul className="ace-selected-list">
                {selected.map((p) => (
                  <li key={p.problemId} className="ace-selected-row">
                    <span className="ace-selected-title">{p.title}</span>
                    <input
                      type="number"
                      min={1}
                      className="ace-score-input"
                      value={p.score}
                      onChange={(e) => updateScore(p.problemId, e.target.value)}
                      title="Points this problem is worth"
                    />
                    <button type="button" className="ace-remove-btn" onClick={() => removeProblem(p.problemId)} aria-label="Remove">
                      ×
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <input
              type="text"
              className="ace-search"
              placeholder="Search problems to add…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

            <div className="ace-problem-list">
              {loadingProblems && <div className="ace-hint">Loading problems…</div>}
              {!loadingProblems && problemsError && <div className="ace-error-inline">{problemsError}</div>}
              {!loadingProblems && !problemsError && filteredProblems.length === 0 && (
                <div className="ace-hint">No problems match "{search}".</div>
              )}
              {!loadingProblems && filteredProblems.map((p) => {
                const diff = DIFF_COLOR[p.difficultyLevel] || { color: '#9CA3AF', label: p.difficultyLevel };
                const isSelected = selected.some((s) => s.problemId === p.problemId);
                const disabled = isSelected || selected.length >= REQUIRED_PROBLEM_COUNT;
                return (
                  <button
                    type="button"
                    key={p.problemId}
                    className={`ace-problem-row ${isSelected ? 'is-selected' : ''}`}
                    disabled={disabled && !isSelected}
                    onClick={() => addProblem(p)}
                  >
                    <span className="ace-problem-title">{p.title}</span>
                    <span className="ace-diff-dot" style={{ background: diff.color }} />
                    <span style={{ color: diff.color }}>{diff.label}</span>
                    <span className="ace-add-mark">{isSelected ? '✓' : '+'}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {formError && <div className="ace-error">{formError}</div>}

          <div className="ace-actions">
            <span className="ace-total">Total score: <strong>{totalScore}</strong> pts</span>
            <button type="submit" className="ace-btn ace-btn--primary" disabled={submitting}>
              {submitting ? 'Creating…' : 'Create exam'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function AdminNavbar({ onLogout }) {
  return (
    <header className="prob-nav">
      <div className="prob-nav-inner">
        <Link to="/" className="prob-logo">
          <span className="logo-icon-p">{'</>'}</span>
          <span>Nextcoder</span>
        </Link>
        <nav className="prob-nav-links">
          <Link to="/problems">Problems</Link>
          <Link to="/contests">Contests</Link>
          <Link to="/admin/exams/create" className="active">Create Exam</Link>
        </nav>
        <div className="prob-nav-right">
          <button className="btn-logout" onClick={onLogout}>Log Out</button>
        </div>
      </div>
    </header>
  );
}
