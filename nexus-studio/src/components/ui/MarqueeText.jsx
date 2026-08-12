import { clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export default function MarqueeText({ items, speed = 'slow', direction = 'forward' }) {
  // We'll rely on the Tailwind config animations for marquee
  const animationClass = direction === 'reverse' ? 'animate-marquee-reverse' : 'animate-marquee'
  
  return (
    <div className="flex overflow-hidden relative w-full">
      <div className={twMerge("flex whitespace-nowrap", animationClass)}>
        {items.map((item, index) => (
          <div key={index} className="flex items-center">
            <span className="mx-8">{item}</span>
            <span className="text-signal/30 text-sm">◆</span>
          </div>
        ))}
        {/* Duplicate items for seamless loop */}
        {items.map((item, index) => (
          <div key={`dup-${index}`} className="flex items-center">
            <span className="mx-8">{item}</span>
            <span className="text-signal/30 text-sm">◆</span>
          </div>
        ))}
      </div>
    </div>
  )
}
