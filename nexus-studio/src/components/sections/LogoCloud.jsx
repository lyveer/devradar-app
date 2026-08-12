import MarqueeText from '../ui/MarqueeText'

export default function LogoCloud() {
  const companies1 = [
    'Vercel', 'Stripe', 'Linear', 'Notion', 'Figma', 
    'Shopify', 'Loom', 'Arc', 'Raycast', 'Pitch'
  ]
  const companies2 = [...companies1].reverse() // the reversed list

  const LogoText = ({ name }) => (
    <span className="font-display text-4xl md:text-5xl font-semibold text-white/20 hover:text-white/80 transition-colors duration-300" data-cursor="hover">
      {name}
    </span>
  )

  const mapped1 = companies1.map(c => <LogoText key={c} name={c} />)
  const mapped2 = companies2.map(c => <LogoText key={c} name={c} />)

  return (
    <section className="bg-ink-900 py-24 pb-32 border-b border-white/5 relative overflow-hidden group">
      <div className="max-w-7xl mx-auto px-6 mb-16 relative z-10">
        <p className="text-center font-mono text-xs text-mist-900 uppercase tracking-widest">
          Trusted by forward-thinking companies
        </p>
      </div>
      
      {/* Optional: Add hover pause to the group */}
      <div className="flex flex-col gap-12 sm:group-hover:[&>div>div]:[animation-play-state:paused] transition-all">
        <MarqueeText items={mapped1} direction="forward" />
        <MarqueeText items={mapped2} direction="reverse" />
      </div>

      {/* Fade edges */}
      <div className="absolute top-0 bottom-0 left-0 w-32 bg-gradient-to-r from-ink-900 to-transparent z-10 pointer-events-none"></div>
      <div className="absolute top-0 bottom-0 right-0 w-32 bg-gradient-to-l from-ink-900 to-transparent z-10 pointer-events-none"></div>
    </section>
  )
}
