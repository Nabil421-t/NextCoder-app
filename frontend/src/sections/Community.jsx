import './Community.css';

const testimonials = [
  {
    quote: "Nextcoder's topic-wise structure helped me crack my first technical interview. The exam mode was the perfect practice ground.",
    name: 'Priya M.',
    role: 'Software Engineer',
  },
  {
    quote: "The instant feedback on submissions is a game-changer. I know exactly where I went wrong and can improve immediately.",
    name: 'Alex R.',
    role: 'CS Student',
  },
  {
    quote: "I love the notification system. I never miss a new contest, and the leaderboard keeps me motivated to push harder.",
    name: 'Jordan T.',
    role: 'Bootcamp Grad',
  },
];

export default function Community() {
  return (
    <section className="section community" id="community">
      <div className="container">
        <div className="eyebrow">COMMUNITY</div>
        <h2 className="section-title">Join a Community of Problem Solvers</h2>

        <div className="testimonials-grid">
          {testimonials.map((t, i) => (
            <div key={i} className="testimonial-card">
              <div className="quote-mark">"</div>
              <p className="testimonial-text">{t.quote}</p>
              <div className="testimonial-author">
                <strong>— {t.name}</strong>, {t.role}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
