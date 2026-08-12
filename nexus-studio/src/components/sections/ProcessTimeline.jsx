import { useRef } from 'react'
import { motion, useScroll, useTransform } from 'framer-motion'
import { useInView } from 'react-intersection-observer'
import ScrollReveal from '../ui/ScrollReveal'

export default function ProcessTimeline() {
  const containerRef = useRef(null)
  const { scrollYProgress } = useScroll({
    target: containerRef,
    offset: ["start center", "end end"]
  })

  // We draw the vertical line from top to bottom
  const scaleY = useTransform(scrollYProgress, [0, 1], [0, 1])

  const steps = [
    { title: "Discovery Call", tagline: "15 minutes that change everything", desc: "A brief alignment on your goals, timeline, constraints, and budget. No pressure, just clarity.", duration: "Day 0" },
    { title: "Deep Dive Workshop", tagline: "We become obsessed with your problem", desc: "We map user journeys, run competitive analysis, and unearth the technical constraints before writing a single line of code.", duration: "Week 1" },
    { title: "Strategy Blueprint", tagline: "Your roadmap to digital dominance", desc: "We deliver a comprehensive architecture, proposed design system foundations, and technical stack choices.", duration: "Week 2" },
    { title: "Design Sprints", tagline: "Pixels become possibilities", desc: "Weekly agile sprints. You get access to live Figma files and daily async updates. Feedback loops are tight and fast.", duration: "Weeks 3-5" },
    { title: "Build & Iterate", tagline: "We ship. You approve. We refine.", desc: "Engineering happens transparently. We push to staging environments continuously so you can test as we build.", duration: "Weeks 4-7" },
    { title: "Launch & Grow", tagline: "The beginning, not the end", desc: "Go-live is orchestrated meticulously. We set up analytics, monitor performance, and hand over the keys (or stay on for support).", duration: "Week 8+" }
  ]

  const StepItem = ({ step, index }) => {
    const { ref, inView } = useInView({ threshold: 0.5 })
    
    return (
      <div ref={ref} className="min-h-screen flex items-center relative py-32" id={`process-step-${index}`}>
        {/* Animated Dot indicator */}
        <div className="absolute left-0 w-8 h-8 -translate-x-1/2 flex items-center justify-center z-10 group cursor-pointer" onClick={() => window.scrollTo({top: document.getElementById(`process-step-${index}`).offsetTop, behavior: 'smooth'})}>
          <motion.div 
            className="w-4 h-4 rounded-full border-2 transition-colors duration-500"
            animate={{ 
              borderColor: inView ? '#e8ff47' : 'rgba(255,255,255,0.2)',
              backgroundColor: inView ? '#e8ff47' : '#080812'
            }}
          />
          <span className="absolute left-8 font-mono text-xs opacity-0 group-hover:opacity-100 transition-opacity text-mist-900">Scroll</span>
        </div>

        <motion.div
          initial={{ x: 60, opacity: 0 }}
          whileInView={{ x: 0, opacity: 1 }}
          viewport={{ margin: '-20%' }}
          transition={{ duration: 0.8, ease: [0.25, 0.46, 0.45, 0.94] }}
          className="pl-16 md:pl-24 relative w-full max-w-2xl"
        >
          {/* Faded Background Number */}
          <div className="absolute -top-12 md:-top-20 left-12 md:left-16 font-display text-[8rem] md:text-[12rem] text-white/5 font-bold leading-none select-none pointer-events-none">
            0{index + 1}
          </div>

          <div className="relative z-10">
            <span className="inline-block border border-white/10 text-mist-900 bg-ink-950 font-mono text-xs px-3 py-1 rounded-full mb-6">
              {step.duration}
            </span>
            <h3 className={`font-display text-4xl md:text-5xl lg:text-6xl font-semibold mb-4 tracking-tight transition-colors duration-500 ${inView ? 'text-white' : 'text-mist-500'}`}>
              {step.title}
            </h3>
            <p className={`font-body text-xl md:text-2xl mb-6 transition-colors duration-500 ${inView ? 'text-signal' : 'text-mist-700'}`}>
              "{step.tagline}"
            </p>
            <p className="text-mist-900 text-base md:text-lg leading-relaxed max-w-lg">
              {step.desc}
            </p>
          </div>
        </motion.div>
      </div>
    )
  }

  return (
    <section ref={containerRef} className="bg-ink-900 relative" id="process">
      <div className="max-w-7xl mx-auto px-6 md:px-12 pt-32 pb-16">
        <ScrollReveal>
          <p className="font-mono text-xs text-signal uppercase tracking-widest mb-4">Process</p>
          <h2 className="font-display text-5xl md:text-7xl font-bold tracking-tight">How we get there.</h2>
        </ScrollReveal>
      </div>

      <div className="max-w-4xl mx-auto px-6 md:px-12 relative w-full flex">
        
        {/* Vertical Timeline container - sticky */}
        <div className="w-px bg-white/5 absolute top-0 bottom-0 left-6 md:left-12 opacity-50"></div>
        
        {/* The active animated line */}
        <div className="sticky top-0 h-screen w-px left-6 md:left-12 flex-shrink-0 z-0">
          <motion.div 
            className="absolute top-0 w-[3px] -ml-[1px] bg-gradient-to-b from-signal/10 via-signal to-signal/10 origin-top shadow-[0_0_15px_rgba(232,255,71,0.5)]"
            style={{ scaleY, height: "100vh" }}
          />
        </div>

        {/* Steps contents */}
        <div className="flex-1 pb-32">
          {steps.map((step, index) => (
            <StepItem key={index} step={step} index={index} />
          ))}
        </div>

      </div>
    </section>
  )
}
