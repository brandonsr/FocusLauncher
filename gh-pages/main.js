/* ===========================
   MAIN.JS — FocusLauncher
   =========================== */

// ── Nav scroll state ──────────────────────────────────────
const nav = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  nav.classList.toggle('scrolled', window.scrollY > 40);
}, { passive: true });

// ── Scroll reveal ─────────────────────────────────────────
const revealEls = document.querySelectorAll(
  '.feature-card, .stat-item, .compare-table, .philosophy-quote, ' +
  '.download-title, .download-subtitle, .download-actions'
);

revealEls.forEach(el => el.classList.add('reveal'));

const revealObserver = new IntersectionObserver((entries) => {
  entries.forEach((entry, i) => {
    if (entry.isIntersecting) {
      // stagger siblings inside the same parent
      const siblings = [...entry.target.parentElement.children]
        .filter(c => c.classList.contains('reveal'));
      const idx = siblings.indexOf(entry.target);
      const delay = idx * 80;

      setTimeout(() => {
        entry.target.classList.add('visible');
      }, delay);

      revealObserver.unobserve(entry.target);
    }
  });
}, { threshold: 0.12 });

revealEls.forEach(el => revealObserver.observe(el));

// ── Animated counters ─────────────────────────────────────
const counterEls = document.querySelectorAll('.stat-number');

const easeOut = (t) => 1 - Math.pow(1 - t, 3);

function animateCounter(el) {
  const target = parseInt(el.dataset.target, 10);
  const duration = 1600;
  let startTime = null;

  function tick(now) {
    if (!startTime) startTime = now;
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);
    el.textContent = Math.round(easeOut(progress) * target);
    if (progress < 1) requestAnimationFrame(tick);
  }

  requestAnimationFrame(tick);
}

const counterObserver = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      animateCounter(entry.target);
      counterObserver.unobserve(entry.target);
    }
  });
}, { threshold: 0.5 });

counterEls.forEach(el => counterObserver.observe(el));

// ── Smooth anchor scrolling (offset for fixed nav) ────────
document.querySelectorAll('a[href^="#"]').forEach(link => {
  link.addEventListener('click', (e) => {
    const target = document.querySelector(link.getAttribute('href'));
    if (!target) return;
    e.preventDefault();
    const offset = 80;
    const top = target.getBoundingClientRect().top + window.scrollY - offset;
    window.scrollTo({ top, behavior: 'smooth' });
  });
});

// ── Subtle cursor glow on hero ────────────────────────────
const heroVisual = document.getElementById('hero-visual');
const phoneGlow = document.querySelector('.phone-glow');

if (heroVisual && phoneGlow) {
  document.addEventListener('mousemove', (e) => {
    const rect = heroVisual.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const maxDist = 200;

    const dx = x - rect.width / 2;
    const dy = y - rect.height / 2;
    const dist = Math.sqrt(dx * dx + dy * dy);

    if (dist < maxDist) {
      const factor = (1 - dist / maxDist) * 20;
      phoneGlow.style.transform = `translate(${dx * 0.05}px, ${dy * 0.05}px) scale(${1 + factor * 0.008})`;
    }
  }, { passive: true });
}
