import './Workspace.css';

export default function Workspace() {
  return (
    <section className="section workspace">
      <div className="container workspace-inner">
        <div className="workspace-left">
          <div className="eyebrow">PROBLEM WORKSPACE</div>
          <h2 className="section-title">Your Code, Your Arena</h2>
          <p className="section-subtitle">
            Every problem opens in a powerful dual-pane workspace. Read the problem
            description on the left, write and test your solution on the right.
          </p>

          <div className="ws-cards">
            <div className="ws-card">
              <div className="ws-card-header">
                <span className="ws-icon">📋</span>
                <strong>Problem Panel</strong>
              </div>
              <p>Full problem description, constraints, examples, and edge cases — always visible while you code.</p>
            </div>
            <div className="ws-card">
              <div className="ws-card-header">
                <span className="ws-icon">⚡</span>
                <strong>Code Editor</strong>
              </div>
              <p>Syntax-highlighted editor with Run and Submit buttons. Write, test, and submit without leaving the page.</p>
            </div>
            <div className="ws-card ws-card-full">
              <div className="ws-card-header">
                <span className="ws-icon">✅</span>
                <strong>Instant Verdict</strong>
              </div>
              <p>Submit and immediately see Accepted, Wrong Answer, or Rejected — along with your score and feedback.</p>
            </div>
          </div>
        </div>

        <div className="workspace-right">
          <div className="dual-pane">
            <div className="pane pane-problem">
              <div className="pane-label">Problem</div>
              <div className="pane-content">
                <div className="pane-line bold" />
                <div className="pane-line" />
                <div className="pane-line short" />
                <div className="pane-divider" />
                <div className="pane-line short" />
                <div className="pane-line" />
                <div className="pane-line short" />
              </div>
            </div>
            <div className="pane pane-editor">
              <div className="pane-label">Editor</div>
              <div className="pane-content code-pane">
                <div className="code-l accent-l" />
                <div className="code-l" />
                <div className="code-l indent" />
                <div className="code-l indent" />
                <div className="code-l" />
                <div className="code-l accent-l" />
                <div className="code-l indent short" />
                <div className="code-l" />
              </div>
              <div className="pane-actions">
                <button className="run-btn">▶ Run</button>
                <button className="submit-btn">Submit</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
