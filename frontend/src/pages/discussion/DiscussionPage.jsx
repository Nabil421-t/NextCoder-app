import { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Flame, 
  Briefcase, 
  Trophy, 
  DollarSign, 
  MessageSquare, 
  HelpCircle, 
  Plus, 
  Search, 
  ThumbsUp, 
  Eye, 
  MoreVertical, 
  Edit3, 
  Trash2, 
  X, 
  CheckCircle2,
  Clock,
  UserCheck,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import Navbar from '../../components/Navbar';
import Footer from '../../components/Footer';
import { 
  getFeed, 
  createPost, 
  updatePost, 
  deletePost 
} from '../../services/postService';
import { getUserIdFromToken, getUserRoleFromToken } from '../../services/api';
import './DiscussionPage.css';

const CATEGORIES = [
  { id: 'for-you', label: 'For You', icon: Flame },
  { id: 'career', label: 'Career', icon: Briefcase },
  { id: 'contest', label: 'Contest', icon: Trophy },
  { id: 'compensation', label: 'Compensation', icon: DollarSign },
  { id: 'feedback', label: 'Feedback', icon: MessageSquare },
  { id: 'interview', label: 'Interview', icon: HelpCircle },
];

// High quality initial sample discussions matching LeetCode feed design
const MOCK_INITIAL_POSTS = [
  {
    postId: 'mock-1',
    postBody: "If AI Had Personalities, Which One Would You Choose?\n\n🌙 A Late-Night Submission Story\nIt's 11:47 PM. You're stuck on a Dynamic Programming problem. Your 17th submission just got Wrong Answer. And, of course... it's today's Daily Challenge. Your brain feels fried, but you can't quit now. What AI coding partner would you want by your side?",
    postAt: '2026-08-03T23:47:00',
    userId: 1,
    username: 'LeetCode',
    fullName: 'LeetCode Team',
    verified: true,
    upvotes: 374,
    views: '1.6K',
    commentsCount: 26,
    category: 'for-you',
    badgeText: 'Coding Lab'
  },
  {
    postId: 'mock-2',
    postBody: "📱 LeetCode at Your Fingertips\n\nIntroducing the LeetCode mobile app, now available for smartphones and tablets. One LeetCode problem a day keeps your reasoning sharp. Jump in for quick practice, browse problem collections, and stay on top of your contest ratings wherever you go!",
    postAt: '2026-04-16T10:00:00',
    userId: 1,
    username: 'LeetCode',
    fullName: 'LeetCode Team',
    verified: true,
    upvotes: 374,
    views: '163K',
    commentsCount: 173,
    category: 'for-you',
    badgeText: 'Announcement'
  },
  {
    postId: 'mock-3',
    postBody: "Amazon | SDE-1 | Selected\n\nI got an offer from Amazon as an SDE-1!\nAbout Me: 2026 Batch - CSE Student College - Tier-1 Experience - 6M internship here, got PPO Offer: Loc - BLR DOJ - August 1st week Base Pay - 19,17,000 Sign-on Bonus Year 1 - 6,47,000",
    postAt: new Date(Date.now() - 4 * 60 * 1000).toISOString(),
    userId: 14,
    username: 'Anonymous User',
    fullName: 'Anonymous User',
    verified: false,
    upvotes: 12,
    views: '16',
    commentsCount: 3,
    category: 'interview',
    badgeText: 'Offer'
  }
];

export default function DiscussionPage() {
  const currentUserId = getUserIdFromToken();
  const userRole = getUserRoleFromToken();

  const [activeTab, setActiveTab] = useState('for-you');
  const [sortOption, setSortOption] = useState('newest'); // 'newest' | 'votes'
  const [searchQuery, setSearchQuery] = useState('');
  
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Pagination state
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingPost, setEditingPost] = useState(null);
  const [postBodyInput, setPostBodyInput] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  // View post modal
  const [selectedPost, setSelectedPost] = useState(null);

  // Upvote states
  const [votedPosts, setVotedPosts] = useState(new Set());

  // Menu dropdown state
  const [activeMenuId, setActiveMenuId] = useState(null);

  useEffect(() => {
    fetchPosts(page, size);
  }, [page, size]);

  async function fetchPosts(currentPage = page, currentSize = size) {
    setLoading(true);
    setError('');
    try {
      const res = await getFeed(currentPage, currentSize);
      const pagedData = res?.data;
      const backendPosts = pagedData?.content || (Array.isArray(pagedData) ? pagedData : []);

      if (pagedData?.totalPages != null) {
        setTotalPages(pagedData.totalPages);
        setTotalElements(pagedData.totalElements || backendPosts.length);
      } else {
        setTotalPages(1);
        setTotalElements(backendPosts.length || MOCK_INITIAL_POSTS.length);
      }

      const formattedBackend = backendPosts.map(p => ({
        ...p,
        upvotes: p.upvotes || 1,
        views: p.views || '24',
        commentsCount: p.commentsCount || 0
      }));

      if (currentPage === 0 && backendPosts.length === 0) {
        setPosts(MOCK_INITIAL_POSTS);
        setTotalPages(1);
        setTotalElements(MOCK_INITIAL_POSTS.length);
      } else {
        setPosts(formattedBackend);
      }
    } catch (err) {
      console.warn('Backend posts fetch failed, falling back to local posts feed:', err);
      setPosts(MOCK_INITIAL_POSTS);
      setTotalPages(1);
      setTotalElements(MOCK_INITIAL_POSTS.length);
    } finally {
      setLoading(false);
    }
  }

  function handleOpenCreate() {
    setEditingPost(null);
    setPostBodyInput('');
    setFormError('');
    setShowCreateModal(true);
  }

  function handleOpenEdit(post, e) {
    e?.stopPropagation();
    setActiveMenuId(null);
    setEditingPost(post);
    setPostBodyInput(post.postBody || '');
    setFormError('');
    setShowCreateModal(true);
  }

  async function handleDelete(postId, e) {
    e?.stopPropagation();
    setActiveMenuId(null);
    if (!window.confirm('Are you sure you want to delete this discussion post?')) return;

    try {
      // If it's a real numeric ID or string from backend, attempt backend delete
      if (typeof postId === 'number' || !String(postId).startsWith('mock-')) {
        await deletePost(postId);
      }
      setPosts(prev => prev.filter(p => p.postId !== postId));
    } catch (err) {
      alert('Failed to delete post: ' + err.message);
    }
  }

  async function handleSubmitPost(e) {
    e.preventDefault();
    if (!postBodyInput.trim()) {
      setFormError('Post body cannot be empty');
      return;
    }
    if (postBodyInput.length > 5000) {
      setFormError('Post body cannot exceed 5000 characters');
      return;
    }

    setSubmitting(true);
    setFormError('');

    try {
      if (editingPost) {
        // Update existing post
        if (typeof editingPost.postId === 'number' || !String(editingPost.postId).startsWith('mock-')) {
          const res = await updatePost(editingPost.postId, { postBody: postBodyInput });
          const updatedData = res?.data || { postBody: postBodyInput };
          setPosts(prev => prev.map(p => p.postId === editingPost.postId ? { ...p, ...updatedData, postBody: postBodyInput } : p));
        } else {
          setPosts(prev => prev.map(p => p.postId === editingPost.postId ? { ...p, postBody: postBodyInput } : p));
        }
      } else {
        // Create new post
        const res = await createPost({ postBody: postBodyInput });
        const newPost = res?.data || {
          postId: Date.now(),
          postBody: postBodyInput,
          postAt: new Date().toISOString(),
          userId: currentUserId || 14,
          username: 'You',
          fullName: 'Current User',
          upvotes: 1,
          views: '1',
          commentsCount: 0
        };
        setPosts(prev => [newPost, ...prev]);
      }
      setShowCreateModal(false);
      setPostBodyInput('');
    } catch (err) {
      setFormError(err.message || 'Failed to submit post');
    } finally {
      setSubmitting(false);
    }
  }

  function toggleUpvote(postId, e) {
    e?.stopPropagation();
    setVotedPosts(prev => {
      const next = new Set(prev);
      const isVoted = next.has(postId);
      if (isVoted) next.delete(postId);
      else next.add(postId);

      setPosts(current => current.map(p => {
        if (p.postId === postId) {
          const currentCount = p.upvotes || 0;
          return { ...p, upvotes: isVoted ? currentCount - 1 : currentCount + 1 };
        }
        return p;
      }));

      return next;
    });
  }

  function getTimestamp(p) {
    const val = p?.postAt || p?.createdAt || p?.updatedAt;
    if (!val) return typeof p?.postId === 'number' ? p.postId : 0;
    if (Array.isArray(val)) {
      const [y, m, d, h = 0, min = 0, s = 0] = val;
      return new Date(y, m - 1, d, h, min, s).getTime();
    }
    const time = new Date(val).getTime();
    return isNaN(time) ? (typeof p?.postId === 'number' ? p.postId : 0) : time;
  }

  // Filtered & sorted posts (Newest post always first)
  const filteredPosts = useMemo(() => {
    return posts.filter(p => {
      const matchesSearch = searchQuery === '' || 
        (p.postBody && p.postBody.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (p.username && p.username.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (p.fullName && p.fullName.toLowerCase().includes(searchQuery.toLowerCase()));

      return matchesSearch;
    }).sort((a, b) => {
      if (sortOption === 'votes') {
        const voteDiff = (b.upvotes || 0) - (a.upvotes || 0);
        if (voteDiff !== 0) return voteDiff;
      }
      return getTimestamp(b) - getTimestamp(a);
    });
  }, [posts, searchQuery, sortOption]);

  // Helper to extract Title & Snippet from postBody
  function parsePostBody(bodyText = '') {
    const lines = bodyText.split('\n').filter(l => l.trim().length > 0);
    const title = lines[0] || 'Discussion Post';
    const snippet = lines.slice(1).join(' ') || bodyText;
    return { title, snippet };
  }

  function formatDate(val) {
    if (!val) return 'Recently';
    try {
      let date;
      if (Array.isArray(val)) {
        const [y, m, d, h = 0, min = 0, s = 0] = val;
        date = new Date(y, m - 1, d, h, min, s);
      } else {
        date = new Date(val);
      }

      if (isNaN(date.getTime())) return 'Recently';

      const now = new Date();
      const diffMs = now - date;
      const diffMins = Math.floor(diffMs / (1000 * 60));
      const diffHours = Math.floor(diffMs / (1000 * 60 * 60));

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins} minutes ago`;
      if (diffHours < 24) return `${diffHours} hours ago`;

      return date.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' });
    } catch (e) {
      return 'Recently';
    }
  }

  return (
    <div className="lc-disc-container">
      <Navbar />

      {/* Category Tabs Sticky Bar */}
      <header className="lc-disc-header">
        <div className="lc-disc-header-inner">
          <div className="lc-disc-tabs">
            {CATEGORIES.map(cat => {
              const Icon = cat.icon;
              const isActive = activeTab === cat.id;
              return (
                <button
                  key={cat.id}
                  className={`lc-tab-btn ${isActive ? 'is-active' : ''}`}
                  onClick={() => setActiveTab(cat.id)}
                >
                  <Icon size={16} />
                  <span>{cat.label}</span>
                </button>
              );
            })}
          </div>

          <button className="lc-create-btn" onClick={handleOpenCreate}>
            <Plus size={16} />
            <span>Create</span>
          </button>
        </div>
      </header>

      {/* Sub-toolbar (Sorting & Search) */}
      <div className="lc-disc-toolbar">
        <div className="lc-sort-group">
          <button
            className={`lc-sort-btn ${sortOption === 'votes' ? 'is-active' : ''}`}
            onClick={() => setSortOption('votes')}
          >
            <ThumbsUp size={14} /> Most Votes
          </button>
          <button
            className={`lc-sort-btn ${sortOption === 'newest' ? 'is-active' : ''}`}
            onClick={() => setSortOption('newest')}
          >
            <Clock size={14} /> Newest
          </button>
        </div>

        <div className="lc-search-box">
          <Search size={15} className="lc-search-icon" />
          <input
            type="text"
            className="lc-search-input"
            placeholder="Search discussions..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {/* Main Feed Content */}
      <main className="lc-disc-feed">
        {loading ? (
          <div className="lc-empty-container">
            <div className="lc-spinner" />
            <p style={{ marginTop: '16px', color: '#8c8c8c' }}>Loading discussions...</p>
          </div>
        ) : filteredPosts.length === 0 ? (
          <div className="lc-empty-container">
            <span className="lc-empty-icon">💬</span>
            <h3 style={{ color: '#fff', fontSize: '18px', fontWeight: 600 }}>No discussions found</h3>
            <p style={{ color: '#8c8c8c', marginTop: '6px', fontSize: '14px' }}>Be the first to share your thoughts or interview experience!</p>
            <button className="lc-create-btn" style={{ marginTop: '16px' }} onClick={handleOpenCreate}>
              <Plus size={16} /> Create Discussion
            </button>
          </div>
        ) : (
          <div className="lc-feed-list">
            {filteredPosts.map(post => {
              const { title, snippet } = parsePostBody(post.postBody);
              const isOwner = currentUserId && (String(post.userId) === String(currentUserId));
              const isAdminUser = userRole && userRole.toUpperCase().includes('ADMIN');
              const canModify = isOwner || isAdminUser;
              const hasVoted = votedPosts.has(post.postId);

              return (
                <article
                  key={post.postId}
                  className="lc-post-card"
                  onClick={() => setSelectedPost(post)}
                >
                  <div className="lc-post-header">
                    <div className="lc-post-author">
                      <div className="lc-avatar">
                        {(post.fullName || post.username || 'A')[0]}
                      </div>
                      <div className="lc-author-info">
                        <span className="lc-author-name">{post.fullName || post.username || 'Anonymous'}</span>
                        {post.verified && (
                          <span className="lc-badge-verified" title="Verified LeetCoder">
                            <CheckCircle2 size={15} />
                          </span>
                        )}
                        <span style={{ color: '#666' }}>·</span>
                        <span className="lc-post-date">{formatDate(post.postAt)}</span>
                      </div>
                    </div>

                    {canModify && (
                      <div className="lc-post-actions-menu" onClick={e => e.stopPropagation()}>
                        <button
                          className="lc-menu-trigger"
                          onClick={() => setActiveMenuId(activeMenuId === post.postId ? null : post.postId)}
                        >
                          <MoreVertical size={18} />
                        </button>
                        {activeMenuId === post.postId && (
                          <div className="lc-dropdown-menu">
                            <button
                              className="lc-dropdown-item"
                              onClick={(e) => handleOpenEdit(post, e)}
                            >
                              <Edit3 size={14} /> Edit
                            </button>
                            <button
                              className="lc-dropdown-item danger"
                              onClick={(e) => handleDelete(post.postId, e)}
                            >
                              <Trash2 size={14} /> Delete
                            </button>
                          </div>
                        )}
                      </div>
                    )}
                  </div>

                  <h2 className="lc-post-title">{title}</h2>
                  <p className="lc-post-snippet">{snippet}</p>

                  <div className="lc-post-footer">
                    <button
                      className={`lc-stat-item ${hasVoted ? 'voted' : ''}`}
                      onClick={(e) => toggleUpvote(post.postId, e)}
                    >
                      <ThumbsUp size={15} />
                      <span>{post.upvotes || 0}</span>
                    </button>

                    <div className="lc-stat-item">
                      <Eye size={15} />
                      <span>{post.views || '1'}</span>
                    </div>

                    <div className="lc-stat-item">
                      <MessageSquare size={15} />
                      <span>{post.commentsCount || 0}</span>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}

        {/* LeetCode Pagination Bar */}
        {totalPages > 0 && filteredPosts.length > 0 && (
          <div className="lc-pagination">
            <div className="lc-pagination-info">
              Showing {page * size + 1}-{Math.min((page + 1) * size, totalElements)} of {totalElements} discussions
            </div>

            <div className="lc-pagination-controls">
              <button
                className="lc-page-btn"
                disabled={page === 0}
                onClick={() => setPage(p => Math.max(0, p - 1))}
              >
                <ChevronLeft size={16} /> Prev
              </button>

              {Array.from({ length: totalPages }, (_, i) => i).map((pNum) => (
                <button
                  key={pNum}
                  className={`lc-page-num ${pNum === page ? 'is-active' : ''}`}
                  onClick={() => setPage(pNum)}
                >
                  {pNum + 1}
                </button>
              ))}

              <button
                className="lc-page-btn"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              >
                Next <ChevronRight size={16} />
              </button>
            </div>

            <div className="lc-pagination-size">
              <select
                value={size}
                onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}
                className="lc-size-select"
              >
                <option value={10}>10 / page</option>
                <option value={20}>20 / page</option>
                <option value={50}>50 / page</option>
              </select>
            </div>
          </div>
        )}
      </main>

      {/* Create / Edit Post Modal */}
      <AnimatePresence>
        {showCreateModal && (
          <div className="lc-modal-backdrop" onClick={() => setShowCreateModal(false)}>
            <div className="lc-modal-card" onClick={e => e.stopPropagation()}>
              <div className="lc-modal-header">
                <h3 className="lc-modal-title">
                  {editingPost ? 'Edit Discussion Post' : 'New Discussion Post'}
                </h3>
                <button className="lc-modal-close" onClick={() => setShowCreateModal(false)}>
                  <X size={20} />
                </button>
              </div>

              <form onSubmit={handleSubmitPost}>
                <div className="lc-modal-body">
                  {formError && (
                    <div style={{ padding: '10px 14px', background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: '8px', color: '#f87171', fontSize: '13px' }}>
                      {formError}
                    </div>
                  )}

                  <textarea
                    className="lc-textarea"
                    placeholder="Write your discussion post here... (e.g. Title on first line, followed by detailed explanation)"
                    value={postBodyInput}
                    onChange={(e) => setPostBodyInput(e.target.value)}
                    maxLength={5000}
                    autoFocus
                  />
                </div>

                <div className="lc-modal-footer">
                  <span className={`lc-char-count ${postBodyInput.length > 4500 ? 'warn' : ''}`}>
                    {postBodyInput.length} / 5000 chars
                  </span>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                      type="button"
                      className="btn-ghost btn-sm text-text-muted"
                      onClick={() => setShowCreateModal(false)}
                      disabled={submitting}
                    >
                      Cancel
                    </button>

                    <button
                      type="submit"
                      className="lc-create-btn"
                      disabled={submitting}
                    >
                      {submitting ? 'Posting...' : editingPost ? 'Update Post' : 'Publish Post'}
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        )}
      </AnimatePresence>

      {/* Detail View Post Modal */}
      <AnimatePresence>
        {selectedPost && (
          <div className="lc-modal-backdrop" onClick={() => setSelectedPost(null)}>
            <div className="lc-modal-card" style={{ maxWidth: '720px' }} onClick={e => e.stopPropagation()}>
              <div className="lc-modal-header">
                <div className="lc-post-author">
                  <div className="lc-avatar">
                    {(selectedPost.fullName || selectedPost.username || 'A')[0]}
                  </div>
                  <div className="lc-author-info">
                    <span className="lc-author-name">{selectedPost.fullName || selectedPost.username || 'Anonymous'}</span>
                    <span style={{ color: '#666' }}>·</span>
                    <span className="lc-post-date">{formatDate(selectedPost.postAt)}</span>
                  </div>
                </div>
                <button className="lc-modal-close" onClick={() => setSelectedPost(null)}>
                  <X size={20} />
                </button>
              </div>

              <div className="lc-modal-body" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                <h2 className="lc-post-title" style={{ fontSize: '20px' }}>
                  {parsePostBody(selectedPost.postBody).title}
                </h2>
                <div style={{ fontSize: '15px', color: '#d1d5db', lineHeight: 1.6, whitespace: 'pre-wrap' }}>
                  {selectedPost.postBody}
                </div>
              </div>

              <div className="lc-modal-footer">
                <div className="lc-post-footer">
                  <button
                    className={`lc-stat-item ${votedPosts.has(selectedPost.postId) ? 'voted' : ''}`}
                    onClick={(e) => toggleUpvote(selectedPost.postId, e)}
                  >
                    <ThumbsUp size={16} />
                    <span>{selectedPost.upvotes || 0} Upvotes</span>
                  </button>
                  <div className="lc-stat-item">
                    <Eye size={16} />
                    <span>{selectedPost.views || '1'} Views</span>
                  </div>
                </div>

                <button className="btn-ghost btn-sm text-text-muted" onClick={() => setSelectedPost(null)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        )}
      </AnimatePresence>

      <Footer />
    </div>
  );
}
