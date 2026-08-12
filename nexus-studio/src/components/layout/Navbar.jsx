import { useState, useEffect } from 'react'
import { motion, AnimatePresence, useScroll, useMotionValueEvent } from 'framer-motion'
import MagneticButton from '../ui/MagneticButton'
import { Menu, X, Camera as Instagram, Briefcase as Linkedin, MessageCircle as Twitter, Palette as Dribbble } from 'lucide-react'

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false)
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const { scrollY } = useScroll()

  useMotionValueEvent(scrollY, "change", (latest) => {
    setScrolled(latest > 80)
  })

  const navLinks = ['Work', 'Services', 'Process', 'Team', 'Blog', 'Contact']
  const socials = [<Linkedin key="ln"/>, <Twitter key="tw"/>, <Instagram key="ig"/>, <Dribbble key="dr"/>]

  return (
    <>
      <header 
        className={`fixed top-0 inset-x-0 z-50 transition-all duration-500 ${
          scrolled ? 'backdrop-blur-xl bg-ink-950/80 border-b border-white/5 py-4' : 'bg-transparent py-6'
        }`}
      >
        <div className="max-w-7xl mx-auto px-6 md:px-12 flex justify-between items-center">
          
          <div className="flex items-center gap-2 cursor-pointer z-50">
            <span className="font-display text-2xl font-bold tracking-tight text-white">NEXUS</span>
            <div className="w-2 h-2 rounded-full bg-signal animate-pulse-slow"></div>
          </div>

          <nav className="hidden md:flex items-center gap-8">
            {navLinks.map((link) => (
              <a 
                key={link} 
                href={`#${link.toLowerCase()}`}
                className="font-body text-sm text-mist-900 hover:text-white transition-colors relative group"
                data-cursor="hover"
              >
                {link}
                <span className="absolute -bottom-1 left-0 h-[1px] bg-signal w-0 group-hover:w-full transition-all duration-300"></span>
              </a>
            ))}
          </nav>

          <div className="hidden md:block">
            <MagneticButton 
              className="px-6 py-2.5 rounded-full border border-ember text-ember text-sm hover:bg-ember hover:text-white transition-colors"
              data-cursor="hover"
            >
              Start a Project
            </MagneticButton>
          </div>

          <button 
            className="md:hidden z-50 text-white p-2"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X size={28} /> : <Menu size={28} />}
          </button>
        </div>
      </header>

      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-ink-900 z-40 flex flex-col justify-center px-6"
          >
            <nav className="flex flex-col gap-6 mt-20">
              {navLinks.map((link, i) => (
                <motion.a
                  key={link}
                  href={`#${link.toLowerCase()}`}
                  initial={{ x: -60, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: i * 0.07, duration: 0.5, ease: [0.25, 0.46, 0.45, 0.94] }}
                  className="font-display text-6xl text-white hover:text-signal transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  {link}
                </motion.a>
              ))}
            </nav>
            <motion.div 
              initial={{ opacity: 0 }} 
              animate={{ opacity: 1 }} 
              transition={{ delay: 0.5 }}
              className="absolute bottom-12 left-6 flex gap-6 text-mist-900"
            >
              {socials.map((icon, i) => (
                <a key={i} href="#" className="hover:text-white">{icon}</a>
              ))}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  )
}
