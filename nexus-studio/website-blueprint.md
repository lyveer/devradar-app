# NEXUS — Service Company Storytelling Website
## Complete Blueprint for Claude Code

---

## 🎯 Project Overview

**Company Name:** NEXUS Studio  
**Tagline:** *"We don't build products. We build futures."*  
**Type:** Premium Service-Based Company (Strategy, Design, Engineering, Growth)  
**Vibe:** Dark luxury editorial — think Apple meets A24 films meets brutalist typography  
**Stack:** React 18 + Vite, Tailwind CSS v3, Framer Motion, Lucide React, shadcn/ui  

---

## 🛠️ Tech Stack & Dependencies

```bash
# Initialize project
npm create vite@latest nexus-studio -- --template react
cd nexus-studio
npm install

# Core dependencies
npm install framer-motion
npm install lucide-react
npm install @radix-ui/react-dialog @radix-ui/react-accordion @radix-ui/react-tabs
npm install clsx tailwind-merge
npm install react-intersection-observer
npm install react-countup
npm install react-router-dom
npm install @radix-ui/react-tooltip

# Tailwind setup
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

---

## 🎨 Design System

### Color Palette (tailwind.config.js)

```js
// tailwind.config.js
module.exports = {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          950: '#04040a',
          900: '#080812',
          800: '#0d0d1f',
          700: '#12122e',
          600: '#1a1a3e',
        },
        signal: {
          DEFAULT: '#e8ff47',   // electric lime — primary accent
          dim: '#b8cc38',
        },
        ember: {
          DEFAULT: '#ff6b35',   // warm orange — secondary accent
          dim: '#cc5529',
        },
        mist: {
          900: '#9898b8',
          700: '#c4c4d8',
          500: '#dcdcec',
          100: '#f0f0f8',
        },
      },
      fontFamily: {
        display: ['"Clash Display"', 'sans-serif'],
        body: ['"Cabinet Grotesk"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      fontSize: {
        '10xl': ['10rem', { lineHeight: '0.9', letterSpacing: '-0.04em' }],
        '9xl':  ['8rem',  { lineHeight: '0.9', letterSpacing: '-0.04em' }],
        '8xl':  ['6rem',  { lineHeight: '0.92', letterSpacing: '-0.03em' }],
      },
      animation: {
        'marquee': 'marquee 25s linear infinite',
        'marquee-reverse': 'marquee 25s linear infinite reverse',
        'float': 'float 6s ease-in-out infinite',
        'pulse-slow': 'pulse 4s ease-in-out infinite',
        'spin-slow': 'spin 20s linear infinite',
      },
      keyframes: {
        marquee: {
          '0%': { transform: 'translateX(0%)' },
          '100%': { transform: 'translateX(-50%)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-20px)' },
        },
      },
      backgroundImage: {
        'noise': "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='1'/%3E%3C/svg%3E\")",
      },
    },
  },
}
```

### Typography Setup

Add to `index.html` `<head>`:
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://api.fontshare.com/v2/css?f[]=clash-display@400,500,600,700&f[]=cabinet-grotesk@400,500,700,800&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
```

### Global CSS (`src/index.css`)

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --cursor-x: 0px;
    --cursor-y: 0px;
  }

  * {
    cursor: none !important;
  }

  html {
    scroll-behavior: smooth;
  }

  body {
    @apply bg-ink-950 text-mist-100 font-body overflow-x-hidden;
    -webkit-font-smoothing: antialiased;
  }

  ::selection {
    @apply bg-signal text-ink-950;
  }
  
  /* Scrollbar */
  ::-webkit-scrollbar { width: 4px; }
  ::-webkit-scrollbar-track { @apply bg-ink-900; }
  ::-webkit-scrollbar-thumb { @apply bg-signal rounded-full; }
}

@layer utilities {
  .text-balance { text-wrap: balance; }
  
  .grain {
    position: relative;
  }
  .grain::after {
    content: '';
    position: absolute;
    inset: 0;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.04'/%3E%3C/svg%3E");
    pointer-events: none;
    z-index: 1;
  }

  .clip-diagonal {
    clip-path: polygon(0 0, 100% 0, 100% 92%, 0 100%);
  }

  .text-stroke {
    -webkit-text-stroke: 1px currentColor;
    color: transparent;
  }
}
```

---

## 📁 Project Structure

```
src/
├── components/
│   ├── ui/
│   │   ├── CustomCursor.jsx
│   │   ├── MagneticButton.jsx
│   │   ├── AnimatedCounter.jsx
│   │   ├── ScrollReveal.jsx
│   │   ├── MarqueeText.jsx
│   │   ├── ParallaxImage.jsx
│   │   └── NoiseBg.jsx
│   ├── layout/
│   │   ├── Navbar.jsx
│   │   └── Footer.jsx
│   └── sections/
│       ├── Hero.jsx
│       ├── LogoCloud.jsx
│       ├── StorySection.jsx
│       ├── ServicesGrid.jsx
│       ├── ProcessTimeline.jsx
│       ├── CaseStudies.jsx
│       ├── TeamSection.jsx
│       ├── TestimonialsCarousel.jsx
│       ├── StatsSection.jsx
│       ├── TechStack.jsx
│       ├── PricingSection.jsx
│       ├── FAQSection.jsx
│       ├── BlogPreview.jsx
│       └── CTASection.jsx
├── hooks/
│   ├── useMousePosition.js
│   ├── useScrollProgress.js
│   └── useInView.js
├── data/
│   └── content.js
├── App.jsx
└── main.jsx
```

---

## 🧩 Component Specifications

---

### 1. `CustomCursor.jsx` — Magnetic Cursor

**Behavior:**
- Default: Small 12×12 white dot
- Hover on links/buttons: Expands to 48×48 circle with "CLICK" text inside
- Hover on images: Turns into a camera lens circle with "VIEW" text
- Hover on text blocks: Thin horizontal line (text cursor)
- Follows mouse with 80ms lag (spring animation via Framer Motion)
- Blend mode: `mix-blend-mode: difference` — inverts colors on dark/light surfaces
- Add a trailing outer ring that follows with more lag (200ms)

```jsx
// Implementation hint
import { motion, useMotionValue, useSpring } from 'framer-motion'
import { useState, useEffect } from 'react'

export default function CustomCursor() {
  const [cursorState, setCursorState] = useState('default') // 'default'|'hover'|'text'|'view'
  const mouseX = useMotionValue(0)
  const mouseY = useMotionValue(0)
  const springX = useSpring(mouseX, { stiffness: 500, damping: 40 })
  const springY = useSpring(mouseY, { stiffness: 500, damping: 40 })
  const trailX = useSpring(mouseX, { stiffness: 150, damping: 25 })
  const trailY = useSpring(mouseY, { stiffness: 150, damping: 25 })

  // cursor size/shape variants based on cursorState
  // Use data-cursor="hover|text|view" attributes on elements
  // Listen to mouseenter/mouseleave on elements with data-cursor attr
}
```

---

### 2. `Navbar.jsx` — Sticky Intelligent Navbar

**Layout:** Fixed top, full width  
**Behavior:**
- Transparent on top of page, dark blur background after 80px scroll
- Logo on left: "NEXUS" in Clash Display font with a blinking green dot
- Center: Navigation links with animated underline (grows from center on hover)
- Right: "Start a Project" CTA — MagneticButton with ember accent
- Mobile: Hamburger → Full-screen overlay menu with staggered link animations
- Active link detection via scroll position

**Nav Links:** Work · Services · Process · Team · Blog · Contact

**Mobile Overlay:**
- Full screen `bg-ink-900`
- Links animate in one by one (stagger 0.1s)
- Large 7xl font for each link
- Social links at bottom

```jsx
// Key classes
// Nav container: fixed top-0 inset-x-0 z-50 px-6 md:px-12 py-5
//   + transition-all duration-500
//   + scrolled ? 'backdrop-blur-xl bg-ink-950/80 border-b border-white/5' : 'bg-transparent'
// Logo: font-display text-xl font-semibold tracking-tight
// Links: font-body text-sm text-mist-900 hover:text-white transition-colors relative group
// Underline: absolute -bottom-0.5 left-0 h-px bg-signal w-0 group-hover:w-full transition-all duration-300
```

---

### 3. `Hero.jsx` — Full-Screen Cinematic Hero

**Layout:** Full viewport height, centered content  
**Background:** 
- Base: `bg-ink-950`
- Animated radial gradient blob that follows mouse cursor (subtle, 40% opacity)
- Fine grid lines overlay: `background-image: linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px)` `background-size: 60px 60px`
- Grain texture overlay at 3% opacity

**Content Structure:**
```
[Top badge: "Available for projects in 2025 →"]
[MASSIVE headline: "We build"]
[MASSIVE headline stroke text: "digital futures"]  ← text-stroke effect
[MASSIVE headline: "that matter."]
[Subheadline paragraph]
[Two CTAs: "See Our Work" (signal) + "How We Work" (ghost)]
[Scroll indicator: animated bouncing arrow]
```

**Typography:**
- Headline: `font-display text-8xl md:text-10xl` — takes up 80% viewport width
- "digital futures" in text-stroke (outline only, transparent fill)
- Staggered word-by-word entrance animation (each word fades + slides up, 0.08s apart)

**Floating Elements:**
- 3 abstract geometric shapes (circles, squares) floating with Framer Motion `animate` looping
- Top-right: Rotating circular badge "PREMIUM · STUDIO · 2025 ·" (CSS spin animation)
- Bottom-left: small stat "48+ Projects Delivered" with animated counter

**Scroll-triggered exit:** Hero content fades + scales down as user scrolls (parallax)

```jsx
// Word-by-word animation
const words = "We build digital futures that matter.".split(' ')
words.map((word, i) => (
  <motion.span
    key={i}
    initial={{ opacity: 0, y: 60, rotateX: -40 }}
    animate={{ opacity: 1, y: 0, rotateX: 0 }}
    transition={{ delay: i * 0.08, duration: 0.7, ease: [0.25, 0.46, 0.45, 0.94] }}
  >
    {word}{' '}
  </motion.span>
))
```

---

### 4. `LogoCloud.jsx` — Trusted By Marquee

**Layout:** Full-width strip, dark background  
**Content:** "Trusted by forward-thinking companies" label above  
**Animation:** Two rows of company logos in infinite marquee
- Row 1: scrolls left
- Row 2: scrolls right (reverse)
- On hover: marquee pauses, hovered logo brightens

**Logos (use SVG text placeholders):** Vercel, Stripe, Linear, Notion, Figma, Shopify, Loom, Arc, Raycast, Pitch (show names as styled text if no actual logos)

**Styling:**
- Logos: white at 25% opacity, 100% opacity on hover
- Separator: small diamond `◆` between each logo
- Infinite CSS animation (no JS)

---

### 5. `StorySection.jsx` — Brand Story / About

**Layout:** Alternating two-column layout (text left, visual right — then flips)  
**This is the storytelling heart of the site.**

**Story Arc (3 chapters):**

**Chapter 01: The Problem**
- Left: Large chapter number `01` in signal color (very faint, huge — decorative)
- Right: Headline "The world drowns in mediocre digital products."
- Body: 2 paragraphs about the status quo problem
- Visual: Abstract "chaos" illustration using CSS shapes

**Chapter 02: Our Belief**  
- Flipped layout
- Headline "We believe every company deserves a world-class digital presence."
- Body: Company philosophy
- Visual: Clean minimal geometric composition

**Chapter 03: What We Do**
- Center layout
- Headline "So we built a studio that does it differently."
- Body: Brief description
- CTA: "Meet the team →"

**Animation:** Each chapter reveals on scroll using Framer Motion `whileInView`  
**Chapter dividers:** Thin horizontal rule with chapter number centered

```jsx
// Section container
<section className="py-32 md:py-48 px-6 md:px-12 max-w-7xl mx-auto">
  {chapters.map((chapter, i) => (
    <motion.div
      key={i}
      initial={{ opacity: 0, y: 80 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-100px' }}
      transition={{ duration: 0.9, ease: [0.25, 0.46, 0.45, 0.94] }}
      className={`grid grid-cols-1 md:grid-cols-2 gap-16 items-center mb-48 ${i % 2 === 1 ? 'md:flex-row-reverse' : ''}`}
    >
```

---

### 6. `ServicesGrid.jsx` — Services Showcase

**Layout:** 4-column grid (2 cols on tablet, 1 on mobile) — **Bento Box style**  
**Each service card is interactive and animated**

**Services (8 cards with varying grid sizes):**
1. **Strategy & Consulting** — Large card (col-span-2) — `bg-ink-800`
2. **UI/UX Design** — Normal card — `bg-ink-700`
3. **Web Development** — Normal card — `bg-ink-700`
4. **Mobile Apps** — Large card (col-span-2) — `bg-ink-800`
5. **Brand Identity** — Normal card — `bg-ink-700`
6. **SEO & Growth** — Normal card — `bg-ink-700`
7. **AI Integration** — Wide card (col-span-3) — signal-tinted
8. **Maintenance & Support** — Normal card — `bg-ink-700`

**Card Anatomy:**
```
[Service number: 01]
[Icon: Lucide icon, white]
[Title: font-display text-2xl]
[Description: 2 lines, mist-900]
[Tags: small pills for tech used]
[Hover: "Explore →" appears, card lifts, border glows]
```

**Card hover animation:**
- Border color transitions from `border-white/5` to `border-signal/50`
- Background lightens slightly
- Arrow icon slides in from left
- Scale: `1.02`

**Bento grid layout hint:**
```jsx
<div className="grid grid-cols-4 gap-4">
  <div className="col-span-2 row-span-2"> {/* Strategy */} </div>
  <div className="col-span-1"> {/* Design */} </div>
  <div className="col-span-1"> {/* Dev */} </div>
  {/* etc */}
</div>
```

---

### 7. `ProcessTimeline.jsx` — How We Work

**Layout:** Vertical timeline on left, content on right (sticky scroll experience)  
**Background:** `bg-ink-900` full section

**Storytelling approach:** Numbered steps that feel like a narrative journey

**Steps:**
1. **Discovery Call** — "15 minutes that change everything" — Understand goals, timeline, budget
2. **Deep Dive Workshop** — "We become obsessed with your problem" — Research, personas, competitor analysis
3. **Strategy Blueprint** — "Your roadmap to digital dominance" — Architecture, design system, tech choices
4. **Design Sprints** — "Pixels become possibilities" — Weekly sprints, live Figma, daily async
5. **Build & Iterate** — "We ship. You approve. We refine." — Agile dev, staging env, QA
6. **Launch & Grow** — "The beginning, not the end" — Go-live, monitoring, ongoing support

**Animation:**
- Active step highlights as you scroll into it (Intersection Observer)
- Timeline line draws itself from top to bottom as you scroll
- Step content slides in from right

**Visual:** Left side has a glowing vertical line, each step has a numbered dot that fills with signal color when active

```jsx
// Sticky scroll implementation
<section className="relative">
  <div className="sticky top-24 h-screen flex items-center">
    {/* Left: timeline line */}
  </div>
  <div className="relative z-10">
    {steps.map(step => (
      <div className="min-h-screen flex items-center">
        {/* Step content */}
      </div>
    ))}
  </div>
</section>
```

---

### 8. `CaseStudies.jsx` — Work / Portfolio

**Layout:** Full-width, horizontal scroll on desktop OR stacked on mobile  
**Background:** Dark with subtle texture

**Case Studies (4 featured):**

**Case 1: Vanta Finance**
- Industry: FinTech
- Service: Full product design + dev
- Result: "3.2x conversion increase"
- Visual: Dark card with ember gradient accent
- Tags: Strategy · Design · React · Node.js

**Case 2: Bloom Health**
- Industry: HealthTech
- Service: Brand identity + web
- Result: "Raised $4M Series A"
- Visual: Card with soft green accent
- Tags: Branding · Framer · SEO

**Case 3: Orbit SaaS**
- Industry: B2B SaaS
- Service: AI dashboard design
- Result: "NPS jumped from 34 to 71"
- Visual: Card with signal accent
- Tags: AI · Design · React

**Case 4: Crest Retail**
- Industry: E-commerce
- Service: Shopify rebuild + growth
- Result: "₹2.4Cr revenue in 90 days"
- Visual: Card with purple accent
- Tags: Shopify · Growth · Analytics

**Card anatomy:**
```
[Background: dark card, full bleed "image" area (abstract CSS gradient)]
[Bottom: company name, result metric in signal color]
[Industry pill, service pills]
[Hover: Overlay with full project description + "View Case Study →"]
[Click: Opens modal or navigates to project page]
```

**Interaction:** Cards have a tilt effect on mouse move (CSS perspective transform)

---

### 9. `StatsSection.jsx` — Numbers / Social Proof

**Layout:** Full-width, `bg-signal` background (the ONE bright section in the whole site)  
**Text color:** `text-ink-950` (dark on bright background — high contrast)

**Stats (animated counters that trigger on scroll):**
- `48+` — Projects Delivered
- `$12M` — Revenue Generated for Clients  
- `98%` — Client Satisfaction Rate
- `4.9★` — Average Clutch Rating
- `6` — Years in Business
- `3` — Countries Served

**Layout:** 3-column grid on desktop, 2-col tablet, 1-col mobile  
**Animation:** Numbers count up when section enters viewport (react-countup)  
**Typography:** Numbers in `font-display text-8xl font-bold`, labels in `font-mono text-sm`

---

### 10. `TeamSection.jsx` — The People

**Layout:** Horizontal scroll OR 4-col grid  
**Concept:** "The humans behind the pixels" — humanizes the brand

**Team Members (4-6):**
- Arjun Mehta — Founder & Strategy Lead
- Priya Kapoor — Creative Director  
- Dev Sharma — Lead Engineer
- Ananya Rao — Growth & Marketing
- Rishi Nair — Motion & 3D Designer
- Zara Khan — Client Success

**Card anatomy:**
```
[Photo area: Abstract colorful CSS art (no real photos needed — use geometric avatars)]
[Name: font-display text-xl]
[Role: font-mono text-sm text-signal]
[One-liner quote in italics]
[Social: LinkedIn + Twitter icons]
```

**Hover:** Card flips to show quote + socials (CSS 3D flip)

---

### 11. `TestimonialsCarousel.jsx` — Client Love

**Layout:** Large centered testimonial with navigation  
**Style:** One testimonial at a time, very large quote text

**Testimonials (5):**
1. "NEXUS transformed our entire digital presence. We went from embarrassed to proud in 12 weeks." — CEO, Vanta Finance
2. "The strategy session alone was worth the entire engagement cost." — Founder, Bloom Health
3. "They think like founders, not vendors. Rare." — CTO, Orbit SaaS
4. "Delivered 3 weeks early. Never happens with agencies." — Product Lead, Crest Retail
5. "Our Clutch review says 5 stars. Honestly, we'd give 6." — CMO, Frameshift

**Animation:**
- Auto-advances every 5s
- Left/right nav arrows (MagneticButton)
- Progress dots at bottom
- Quote text fades and slides on change
- Company name + role below
- Large decorative `"` in signal color behind quote

---

### 12. `TechStack.jsx` — Technologies

**Layout:** Scrolling pill cloud OR organized grid  
**Concept:** Grouped by category with animated hover pills

**Categories:**
- **Frontend:** React, Next.js, TypeScript, Framer Motion, Three.js
- **Backend:** Node.js, Python, FastAPI, PostgreSQL, Redis
- **Cloud:** AWS, Vercel, Docker, Kubernetes
- **Design:** Figma, Adobe Suite, Spline, Rive
- **AI/ML:** OpenAI, Langchain, Pinecone, HuggingFace

**Each pill:** 
```jsx
<motion.div 
  whileHover={{ scale: 1.05, backgroundColor: '#e8ff47', color: '#04040a' }}
  className="px-4 py-2 rounded-full border border-white/10 text-sm font-mono"
>
  {tech}
</motion.div>
```

**Layout:** Use `flex flex-wrap gap-3` with pills of varying widths

---

### 13. `PricingSection.jsx` — Investment / Pricing

**Layout:** 3-column cards  
**Concept:** Honest, transparent pricing builds trust

**Packages:**

**Starter — ₹1.5L**
- Perfect for: Startups & MVPs
- Includes: Brand + 5-page website + 3 months support
- Timeline: 3 weeks
- Border: white/10

**Growth — ₹4L** ← POPULAR
- Perfect for: Scaling companies
- Includes: Full design system + web app + SEO + 6 months support
- Timeline: 6 weeks
- Border: signal — highlighted card, slightly larger
- Badge: "Most Popular" pill

**Enterprise — Custom**
- Perfect for: Series A+ companies
- Includes: Everything + dedicated team + AI features + priority support
- Timeline: Custom
- Border: ember

**Card anatomy:**
```
[Package name]
[Price / "Custom"]
[One-liner positioning]
[Feature list with checkmarks (signal colored) — 6-8 items]
[CTA button]
[Fine print: "No hidden fees. Cancel anytime."]
```

**Hover:** Card lifts, border glows, CTA button pulses

---

### 14. `FAQSection.jsx` — Frequently Asked Questions

**Layout:** Left: large "FAQ" text, Right: Accordion  
**Component:** Use Radix UI Accordion for accessibility

**Questions (8):**
1. How long does a typical project take?
2. Do you work with international clients?
3. What's your revision policy?
4. Do you offer payment plans?
5. Can I hire just for design, or development separately?
6. Do you sign NDAs?
7. What happens after the project is delivered?
8. How do you handle urgent or rush projects?

**Accordion style:**
- Default: closed, question text in white, `+` icon
- Open: question in signal, answer slides down, `−` icon
- Border bottom only on each item
- Smooth height animation via Framer Motion AnimatePresence

---

### 15. `BlogPreview.jsx` — Insights / Blog

**Layout:** 3-column card grid  
**Concept:** "We share what we know" — builds authority

**Blog Posts (3):**

**Post 1:**
- Title: "Why 90% of SaaS products fail at onboarding (and how to fix it)"
- Category: UX Strategy
- Read time: 7 min
- Date: Jan 2025

**Post 2:**
- Title: "The Indian startup design deficit: a ₹500Cr opportunity"
- Category: Industry
- Read time: 5 min
- Date: Feb 2025

**Post 3:**
- Title: "AI won't replace designers. But it will replace bad designers."
- Category: AI & Design
- Read time: 9 min
- Date: Mar 2025

**Card anatomy:**
```
[Category pill (signal outline)]
[Title: font-display text-xl leading-tight]
[Excerpt: 2 lines, mist-900]
[Author avatar + name + date + read time]
[Hover: "Read Article →" slides in, card border glows]
```

---

### 16. `CTASection.jsx` — Final Call to Action

**Layout:** Full-screen, centered, dramatic  
**This is the emotional climax of the page.**

**Concept:** Big, bold, impossible to ignore

**Content:**
```
[Small text: "Ready to build something great?"]
[MASSIVE headline: "Let's make"]
[MASSIVE headline stroke: "it happen."]
[Body: One sentence about first step]
[Primary CTA: "Book a Free Call" — large button]
[Secondary: "Or email us at hello@nexus.studio"]
[Bottom: Social proof — "Join 48 companies who chose us"]
```

**Background:**
- Full bleed image-quality gradient (deep purple to ink)
- Animated noise grain
- Floating abstract shapes (very subtle, large, slow)
- Radial spotlight effect behind headline

**CTA Button:**
- Large: `px-12 py-6 text-lg`
- `bg-signal text-ink-950 font-display font-semibold`
- MagneticButton component
- On hover: rotates slightly, shadow pulses

---

### 17. `Footer.jsx` — Footer

**Layout:** 4-column grid + bottom bar  
**Background:** `bg-ink-950` with top border in white/5

**Columns:**
- **Col 1:** Logo + 2-line tagline + social icons (LinkedIn, Twitter/X, Instagram, Dribbble)
- **Col 2 — Services:** List of all service links
- **Col 3 — Company:** About, Process, Team, Blog, Careers, Press
- **Col 4 — Contact:** Email, Phone, Address (Bangalore, India), Map link

**Bottom bar:**
- Left: © 2025 NEXUS Studio. All rights reserved.
- Center: "Designed with ♥ in Bangalore"
- Right: Privacy Policy · Terms · Sitemap

**Footer easter egg:** Hovering the logo plays a subtle animation. Clicking the year (2025) shows a tooltip: "Our founding year — and still going strong."

---

## 🎬 Animation Guidelines

### Global Principles
1. **Entrance:** All sections use `whileInView` with `viewport={{ once: true, margin: '-80px' }}`
2. **Easing:** Always use `ease: [0.25, 0.46, 0.45, 0.94]` (custom ease-out-expo)
3. **Duration:** 0.6–0.9s for major reveals, 0.2–0.3s for micro-interactions
4. **Stagger:** 0.08–0.12s between child elements

### Standard Reveal Variants
```js
// In src/utils/animations.js
export const fadeUpVariants = {
  hidden: { opacity: 0, y: 60 },
  visible: (i = 0) => ({
    opacity: 1,
    y: 0,
    transition: {
      delay: i * 0.1,
      duration: 0.8,
      ease: [0.25, 0.46, 0.45, 0.94],
    },
  }),
}

export const fadeInVariants = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { duration: 0.6 } },
}

export const scaleUpVariants = {
  hidden: { opacity: 0, scale: 0.92 },
  visible: { opacity: 1, scale: 1, transition: { duration: 0.7, ease: [0.25, 0.46, 0.45, 0.94] } },
}

export const staggerContainerVariants = {
  hidden: {},
  visible: { transition: { staggerChildren: 0.08 } },
}
```

### Scroll Progress Bar
Add a thin signal-colored line at the very top of the page that fills as you scroll down (use `useScroll` from Framer Motion + `scaleX` on a fixed element).

---

## 📱 Responsive Breakpoints

| Breakpoint | Width | Key Changes |
|---|---|---|
| Mobile | < 640px | 1-col layout, no cursor, simplified hero |
| Tablet | 640–1024px | 2-col grid, horizontal scroll for case studies |
| Desktop | > 1024px | Full layout, sticky timeline, custom cursor |
| Large | > 1440px | Max-width container (1400px), more whitespace |

---

## 🔧 Utility Components

### `MagneticButton.jsx`
```jsx
// Button that subtly moves toward cursor on hover
// Use for all primary CTAs
import { useRef } from 'react'
import { motion, useMotionValue, useSpring, useTransform } from 'framer-motion'

export default function MagneticButton({ children, className, ...props }) {
  const ref = useRef(null)
  const x = useMotionValue(0)
  const y = useMotionValue(0)
  const springX = useSpring(x, { stiffness: 200, damping: 15 })
  const springY = useSpring(y, { stiffness: 200, damping: 15 })

  const handleMouseMove = (e) => {
    const rect = ref.current.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2
    x.set((e.clientX - centerX) * 0.35)
    y.set((e.clientY - centerY) * 0.35)
  }

  return (
    <motion.button
      ref={ref}
      style={{ x: springX, y: springY }}
      onMouseMove={handleMouseMove}
      onMouseLeave={() => { x.set(0); y.set(0) }}
      className={className}
      {...props}
    >
      {children}
    </motion.button>
  )
}
```

### `ScrollReveal.jsx`
```jsx
import { motion } from 'framer-motion'
export default function ScrollReveal({ children, delay = 0, className }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 50 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-80px' }}
      transition={{ duration: 0.8, delay, ease: [0.25, 0.46, 0.45, 0.94] }}
      className={className}
    >
      {children}
    </motion.div>
  )
}
```

### `MarqueeText.jsx`
```jsx
// Infinite scroll text strip
// Usage: <MarqueeText items={['Strategy', 'Design', 'Development', 'Growth']} />
// Add ◆ separator between items
// Two divs side-by-side, each with animation-marquee, second with animation-marquee-reverse
```

### `AnimatedCounter.jsx`
```jsx
import CountUp from 'react-countup'
import { useInView } from 'react-intersection-observer'
export default function AnimatedCounter({ end, prefix = '', suffix = '' }) {
  const [ref, inView] = useInView({ triggerOnce: true })
  return (
    <span ref={ref}>
      {inView && <CountUp start={0} end={end} duration={2.5} prefix={prefix} suffix={suffix} />}
    </span>
  )
}
```

---

## 📋 Content Data (`src/data/content.js`)

```js
export const services = [
  { id: 1, number: '01', icon: 'Compass', title: 'Strategy & Consulting', desc: 'We map your business goals to digital outcomes with ruthless clarity.', tags: ['Research', 'Roadmap', 'OKRs'], span: 'col-span-2' },
  { id: 2, number: '02', icon: 'Paintbrush', title: 'UI/UX Design', desc: 'Interfaces that feel inevitable — like they were always meant to exist.', tags: ['Figma', 'Design Systems', 'Prototyping'], span: 'col-span-1' },
  { id: 3, number: '03', icon: 'Code2', title: 'Web Development', desc: 'We build fast, accessible, and scalable web applications.', tags: ['React', 'Next.js', 'Node.js'], span: 'col-span-1' },
  { id: 4, number: '04', icon: 'Smartphone', title: 'Mobile Apps', desc: 'Native and cross-platform apps your users will actually love using.', tags: ['React Native', 'iOS', 'Android'], span: 'col-span-2' },
  { id: 5, number: '05', icon: 'Layers', title: 'Brand Identity', desc: 'Your brand is a story. We make sure it is one worth telling.', tags: ['Logo', 'Guidelines', 'Assets'], span: 'col-span-1' },
  { id: 6, number: '06', icon: 'TrendingUp', title: 'SEO & Growth', desc: 'Organic strategies that compound over time like a good investment.', tags: ['SEO', 'Analytics', 'CRO'], span: 'col-span-1' },
  { id: 7, number: '07', icon: 'Cpu', title: 'AI Integration', desc: 'We embed intelligence into your product — not as a feature, but as a foundation.', tags: ['OpenAI', 'LangChain', 'Vector DBs', 'Fine-tuning'], span: 'col-span-3' },
  { id: 8, number: '08', icon: 'Shield', title: 'Maintenance & Support', desc: 'We do not ghost after launch. Your success is our reputation.', tags: ['SLA', 'Monitoring', 'Updates'], span: 'col-span-1' },
]

export const caseStudies = [
  { id: 1, company: 'Vanta Finance', industry: 'FinTech', result: '3.2× Conversion', desc: 'Redesigned their onboarding flow from 14 steps to 3. Conversion tripled in 6 weeks.', services: ['Strategy', 'Design', 'React'], accentColor: 'from-ember/20 to-transparent' },
  { id: 2, company: 'Bloom Health', industry: 'HealthTech', result: '$4M Series A', desc: 'Built the investor-facing brand and product demo that closed their seed round.', services: ['Branding', 'Web', 'Pitch Deck'], accentColor: 'from-green-900/40 to-transparent' },
  { id: 3, company: 'Orbit SaaS', industry: 'B2B SaaS', result: 'NPS 34 → 71', desc: 'Redesigned the core dashboard with AI-assisted insights. Users finally understood their data.', services: ['AI', 'Design', 'React'], accentColor: 'from-signal/10 to-transparent' },
  { id: 4, company: 'Crest Retail', industry: 'E-commerce', result: '₹2.4Cr / 90 days', desc: 'Shopify rebuild with conversion-first design. Revenue target hit in under 3 months.', services: ['Shopify', 'Growth', 'SEO'], accentColor: 'from-purple-900/40 to-transparent' },
]

export const team = [
  { name: 'Arjun Mehta', role: 'Founder & Strategy', quote: 'Good strategy is just clear thinking made visible.', colors: ['#e8ff47', '#080812'] },
  { name: 'Priya Kapoor', role: 'Creative Director', quote: 'Design that doesn\'t solve a problem is just decoration.', colors: ['#ff6b35', '#080812'] },
  { name: 'Dev Sharma', role: 'Lead Engineer', quote: 'Code is poetry. Ship it like it is.', colors: ['#a78bfa', '#080812'] },
  { name: 'Ananya Rao', role: 'Growth & Marketing', quote: 'Growth is a system, not a hack.', colors: ['#34d399', '#080812'] },
]

export const faqs = [
  { q: 'How long does a typical project take?', a: 'Most projects take 3–8 weeks depending on scope. We\'ll give you a precise timeline in our discovery call. We don\'t pad timelines — we hit them.' },
  { q: 'Do you work with international clients?', a: 'Yes. About 30% of our clients are outside India. We work async-first with tools like Linear, Figma, and Loom — timezone is rarely a barrier.' },
  { q: 'What\'s your revision policy?', a: 'Unlimited revisions within scope. We\'ve never had a client feel they ran out of revisions, because we align on direction early.' },
  { q: 'Do you offer payment plans?', a: 'Yes. Typically 40% upfront, 30% at midpoint, 30% on delivery. For larger engagements we can structure monthly retainers.' },
  { q: 'Can I hire just for design, or development separately?', a: 'Absolutely. Many clients start with design-only, then bring us in for development later. Others need just a technical build from existing designs.' },
  { q: 'Do you sign NDAs?', a: 'Yes, always. We treat client information with the same care we give our own.' },
  { q: 'What happens after the project is delivered?', a: 'All projects include 30 days of post-launch support at no extra charge. After that, we offer monthly retainer plans starting at ₹25,000/month.' },
  { q: 'How do you handle urgent or rush projects?', a: 'We have a rush lane for time-sensitive projects (1.35× standard rate). Talk to us — we\'ve launched products in 7 days when the stakes demanded it.' },
]
```

---

## 🚀 App.jsx — Page Assembly

```jsx
import CustomCursor from './components/ui/CustomCursor'
import ScrollProgressBar from './components/ui/ScrollProgressBar'
import Navbar from './components/layout/Navbar'
import Footer from './components/layout/Footer'
import Hero from './components/sections/Hero'
import LogoCloud from './components/sections/LogoCloud'
import StorySection from './components/sections/StorySection'
import ServicesGrid from './components/sections/ServicesGrid'
import ProcessTimeline from './components/sections/ProcessTimeline'
import CaseStudies from './components/sections/CaseStudies'
import StatsSection from './components/sections/StatsSection'
import TeamSection from './components/sections/TeamSection'
import TestimonialsCarousel from './components/sections/TestimonialsCarousel'
import TechStack from './components/sections/TechStack'
import PricingSection from './components/sections/PricingSection'
import FAQSection from './components/sections/FAQSection'
import BlogPreview from './components/sections/BlogPreview'
import CTASection from './components/sections/CTASection'

export default function App() {
  return (
    <div className="relative">
      <CustomCursor />
      <ScrollProgressBar />
      <Navbar />
      <main>
        <Hero />
        <LogoCloud />
        <StorySection />
        <ServicesGrid />
        <ProcessTimeline />
        <CaseStudies />
        <StatsSection />
        <TeamSection />
        <TestimonialsCarousel />
        <TechStack />
        <PricingSection />
        <FAQSection />
        <BlogPreview />
        <CTASection />
      </main>
      <Footer />
    </div>
  )
}
```

---

## ⚡ Performance Checklist

- [ ] All images use `loading="lazy"` and `decoding="async"`
- [ ] Framer Motion: use `whileInView` over scroll listeners
- [ ] Use `will-change: transform` only on actively animating elements
- [ ] Split code by route if adding multiple pages later
- [ ] Fonts preloaded in `<head>`
- [ ] No layout shift — define min-heights on skeleton sections

---

## 🎯 Section Order & Storytelling Flow

```
AWARENESS     →  Hero (Who are we / what do we do)
TRUST         →  LogoCloud (Who trusts us)
CONNECTION    →  StorySection (Why we exist)
SOLUTION      →  ServicesGrid (What we offer)
PROCESS       →  ProcessTimeline (How we work)
PROOF         →  CaseStudies (What we've done)
NUMBERS       →  StatsSection (Scale of impact)
HUMAN         →  TeamSection (Who's behind it)
VALIDATION    →  Testimonials (What clients say)
CAPABILITY    →  TechStack (How we build)
DECISION      →  PricingSection (What it costs)
OBJECTIONS    →  FAQSection (Removing friction)
AUTHORITY     →  BlogPreview (We share knowledge)
ACTION        →  CTASection (Take the leap)
```

---

## 📌 Final Notes for Claude Code

1. **Build each component in isolation** before assembling in App.jsx
2. **Start with the design system** (tailwind.config.js + index.css) before any components
3. **CustomCursor is global** — render it at the App level, not inside sections
4. **All section backgrounds should be slightly different** to create visual rhythm as you scroll — alternate between `bg-ink-950`, `bg-ink-900`, `bg-ink-800`
5. **Signal color (`#e8ff47`) is used sparingly** — only for the most important highlights. Don't overuse it.
6. **Every CTA button should be a MagneticButton** component
7. **Mobile-first** — build responsive from the start, not as an afterthought
8. **Test animations on low-power mode** — use `prefers-reduced-motion` media query to disable animations for accessibility

```jsx
// Add this to framer-motion animations
const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
// If true, skip all animation delays and use opacity-only transitions
```

---

*Blueprint version 1.0 — Built for Claude Code*  
*Estimated build time: 8–12 hours (full implementation)*  
*Complexity: Advanced*
