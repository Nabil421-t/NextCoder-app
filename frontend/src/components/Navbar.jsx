import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, Search, Menu, X, User, ShieldCheck } from 'lucide-react';
import { isAdmin, getUserIdFromToken } from '../services/api';

const NAV_LINKS = [
  { to: '/problems', label: 'Problems' },
  { to: '/contests', label: 'Contests' },
  { to: '/discussion', label: 'Discussion' },
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/notifications', label: 'Notifications' },
];

export default function Navbar() {
  const admin = isAdmin();
  const loggedIn = !!getUserIdFromToken();
  const location = useLocation();
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8);
    onScroll();
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => setMobileOpen(false), [location.pathname]);

  const links = admin
    ? [...NAV_LINKS, { to: '/admin/exams/create', label: 'Create Exam' }]
    : NAV_LINKS;

  return (
    <motion.header
      initial={{ y: -24, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
      className={`sticky top-0 z-[100] transition-all duration-300 ${
        scrolled
          ? 'bg-bg/70 backdrop-blur-xl border-b border-white/10 shadow-[0_4px_30px_-8px_rgba(0,0,0,0.5)]'
          : 'bg-transparent border-b border-transparent'
      }`}
    >
      <div className="container flex items-center justify-between h-[68px] gap-8">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 shrink-0 group">
          <motion.span
            whileHover={{ rotate: -6, scale: 1.05 }}
            transition={{ type: 'spring', stiffness: 300, damping: 15 }}
            className="grid place-items-center w-9 h-9 rounded-[10px] bg-gradient-to-br from-primary to-secondary text-white font-mono font-bold text-sm shadow-glow"
          >
            {'</>'}
          </motion.span>
          <span className="font-display font-bold text-lg tracking-tight text-text">
            Nextcoder
          </span>
        </Link>

        {/* Desktop nav */}
        <nav className="hidden md:flex items-center gap-1 flex-1 justify-center">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `relative px-4 py-2 text-sm font-medium rounded-full transition-colors ${
                  isActive ? 'text-text' : 'text-text-muted hover:text-text'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  {link.label}
                  {isActive && (
                    <motion.span
                      layoutId="navbar-active-pill"
                      className="absolute inset-0 -z-10 rounded-full bg-white/[0.06] border border-white/10"
                      transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                    />
                  )}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* Actions */}
        <div className="hidden md:flex items-center gap-2">
          <button
            aria-label="Search"
            className="grid place-items-center w-9 h-9 rounded-full text-text-muted hover:text-text hover:bg-white/[0.06] transition-colors"
          >
            <Search size={18} />
          </button>
          <Link
            to="/notifications"
            aria-label="Notifications"
            className="relative grid place-items-center w-9 h-9 rounded-full text-text-muted hover:text-text hover:bg-white/[0.06] transition-colors"
          >
            <Bell size={18} />
            <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 rounded-full bg-accent" />
          </Link>

          {admin && (
            <motion.div
              whileHover={{ scale: 1.05 }}
              className="grid place-items-center w-9 h-9 rounded-full bg-gradient-to-br from-primary to-secondary text-white ml-1"
            >
              <ShieldCheck size={16} />
            </motion.div>
          )}
          <Link to="/login" className="btn-outline btn-sm ml-1 text-white" style={{ color: '#fff' }}>
            Log In
          </Link>
          <Link to="/register" className="btn-primary btn-sm text-white" style={{ color: '#fff' }}>
            Get Started Free
          </Link>
        </div>

        {/* Mobile toggle */}
        <button
          className="md:hidden grid place-items-center w-9 h-9 text-text"
          onClick={() => setMobileOpen((v) => !v)}
          aria-label="Toggle menu"
        >
          {mobileOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {/* Mobile menu */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.nav
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
            className="md:hidden overflow-hidden bg-bg/95 backdrop-blur-xl border-b border-white/10"
          >
            <div className="container flex flex-col gap-1 py-4">
              {links.map((link) => (
                <Link
                  key={link.to}
                  to={link.to}
                  className="px-3 py-2.5 rounded-lg text-sm font-medium text-text-muted hover:text-text hover:bg-white/[0.06]"
                >
                  {link.label}
                </Link>
              ))}
              <div className="flex gap-2 mt-2">
                {loggedIn && (
                  <span className="text-sm text-text-muted px-3 py-2">Signed in</span>
                )}
                <Link to="/login" className="btn-outline btn-sm flex-1 text-center text-white" style={{ color: '#fff' }}>
                  Log In
                </Link>
                <Link to="/register" className="btn-primary btn-sm flex-1 text-center text-white" style={{ color: '#fff' }}>
                  Get Started
                </Link>
              </div>
            </div>
          </motion.nav>
        )}
      </AnimatePresence>
    </motion.header>
  );
}
