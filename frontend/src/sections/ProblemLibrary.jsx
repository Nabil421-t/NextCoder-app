import './ProblemLibrary.css';

const topics = [
  { name: 'Arrays & Strings', count: 120 },
  { name: 'Dynamic Programming', count: 85 },
  { name: 'Graphs & Trees', count: 95 },
  { name: 'Sorting & Search', count: 60 },
];

const difficultyColors = {
  EASY: '#27C93F',
  MEDIUM: '#FFBD2E',
  HARD: '#FF5F56',
};

const sampleProblems = [
  { name: 'Two Sum', diff: 'EASY' },
  { name: 'Longest Common Subsequence', diff: 'MEDIUM' },
  { name: 'Minimum Spanning Tree', diff: 'HARD' },
  { name: 'Binary Search Variants', diff: 'MEDIUM' },
  { name: 'Graph Coloring', diff: 'HARD' },
];

export default function ProblemLibrary() {
  return (
    <section className="section problem-library" id="problems">
      <div className="container">
        <div className="eyebrow">PROBLEM LIBRARY</div>
        <h2 className="section-title">Thousands of Problems. Organized Your Way.</h2>

        <div className="library-inner">
          <div className="library-left">
            <h3 className="lib-subtitle">Topic-Wise Navigation</h3>
            <p className="lib-desc">
              Browse problems by category. Click any topic to reveal a full list of curated
              challenges — from beginner-friendly to expert-level.
            </p>

            <div className="topic-grid">
              {topics.map((t, i) => (
                <div key={i} className="topic-card">
                  <div className="topic-name">{t.name}</div>
                  <div className="topic-count">{t.count} problems</div>
                </div>
              ))}
            </div>
          </div>

          <div className="library-right">
            <div className="problem-list-window">
              <div className="plw-header">
                <span className="plw-title">📋 Coding Shop</span>
                <span className="plw-sub">Practice Problems</span>
              </div>
              <div className="plw-body">
                {sampleProblems.map((p, i) => (
                  <div key={i} className="plw-row">
                    <span className="plw-name">{p.name}</span>
                    <span
                      className="plw-diff"
                      style={{ color: difficultyColors[p.diff] }}
                    >
                      {p.diff}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
