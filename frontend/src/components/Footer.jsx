import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="footer-logo">
          <span className="logo-icon">{'</>'}</span>
          <span>Nextcoder</span>
        </div>
        <div className="footer-links">
          <a href="#">Problems</a>
          <a href="#">Contests</a>
          <a href="#">Leaderboard</a>
          <a href="#">Privacy</a>
          <a href="#">Terms</a>
        </div>
        <div className="footer-copy">© 2025 Nextcoder. All rights reserved.</div>
      </div>
    </footer>
  );
}
