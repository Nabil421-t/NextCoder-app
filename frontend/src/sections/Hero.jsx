import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ChevronDown, Flame, Trophy, Play } from 'lucide-react';
import './Hero.css';

const LANGS = [
  { label: 'JS', color: 'bg-warning/15 text-warning' },
  { label: 'PY', color: 'bg-accent/15 text-accent' },
  { label: 'JAVA', color: 'bg-danger/15 text-danger' },
  { label: 'C++', color: 'bg-primary/15 text-primary-light' },
];

export default function Hero() {
  return (
    <section className="hero relative overflow-hidden">
      {/* animated background blobs */}
      <div className="bg-blob w-[420px] h-[420px] bg-primary/40 -top-32 -left-20" />
      <div
        className="bg-blob w-[360px] h-[360px] bg-secondary/40 top-40 right-0"
        style={{ animationDelay: '3s' }}
      />
      <div
        className="bg-blob w-[300px] h-[300px] bg-accent/30 bottom-0 left-1/3"
        style={{ animationDelay: '6s' }}
      />

      <div className="container hero-inner relative z-10">
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
          className="hero-content"
        >
          <span className="eyebrow">🏆 Competitive Programming Platform</span>
          <h1 className="hero-title">
            Master Every Algorithm
            <br />
            <span className="gradient-text">One Problem at a Time.</span>
          </h1>
          <p className="hero-desc">
            Practice curated problems across 10 core topics — from Arrays to Segment Trees —
            in a clean, LeetCode-style interface. Write code, run test cases, and submit
            solutions with instant feedback.
          </p>
          <div className="hero-actions">
            <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.97 }}>
              <Link to="/register" className="btn-primary">Start Solving</Link>
            </motion.div>
            <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.97 }}>
              <Link to="/problems" className="btn-outline">Browse All Topics</Link>
            </motion.div>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.15, ease: [0.16, 1, 0.3, 1] }}
          className="hero-visual"
        >
          {/* language dropdown floating chip */}
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.6, duration: 0.4 }}
            style={{ animation: 'floatSlow 5s ease-in-out infinite' }}
            className="lang-chip glass"
          >
            <span className="text-sm font-mono text-text">Java</span>
            <ChevronDown size={14} className="text-text-muted" />
            <div className="lang-chip-menu">
              {LANGS.map((l) => (
                <span key={l.label} className={`lang-pill ${l.color}`}>{l.label}</span>
              ))}
            </div>
          </motion.div>

          {/* streak / rank floating badges */}
          <motion.div
            initial={{ opacity: 0, x: -16 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.75, duration: 0.4 }}
            style={{ animation: 'floatSlow 6s ease-in-out infinite 0.5s' }}
            className="float-badge float-badge--left glass"
          >
            <span className="float-badge-icon bg-warning/15 text-warning"><Flame size={16} /></span>
            <div>
              <div className="text-sm font-semibold text-text leading-tight">12-day streak</div>
              <div className="text-xs text-text-muted">Keep it going 🔥</div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, x: 16 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.9, duration: 0.4 }}
            style={{ animation: 'floatSlow 5.5s ease-in-out infinite 1s' }}
            className="float-badge float-badge--right glass"
          >
            <span className="float-badge-icon bg-primary/15 text-primary-light"><Trophy size={16} /></span>
            <div>
              <div className="text-sm font-semibold text-text leading-tight">Rank #482</div>
              <div className="text-xs text-text-muted">Global leaderboard</div>
            </div>
          </motion.div>

          <div className="code-window">
            <div className="window-bar">
              <span className="dot red" /><span className="dot yellow" /><span className="dot green" />
              <span className="window-title">solution.py</span>
              <motion.span
                whileHover={{ scale: 1.08 }}
                whileTap={{ scale: 0.95 }}
                className="run-chip"
              >
                <Play size={11} fill="currentColor" /> Run
              </motion.span>
            </div>
            <div className="code-body">
              <pre className="code-text">{`def twoSum(nums: list[int], target: int) -> list[int]:
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []

# Example:
# Input:  nums = [2,7,11,15], target = 9
# Output: [0, 1]`}</pre>
            </div>
            <div className="verdict accepted">✓ Accepted — 100/100</div>
          </div>
          <div className="hero-glow" />
        </motion.div>
      </div>
    </section>
  );
}
