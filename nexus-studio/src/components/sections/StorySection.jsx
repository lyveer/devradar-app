import { motion } from 'framer-motion'

export default function StorySection() {
  const chapters = [
    {
      num: '01',
      title: 'The world drowns in mediocre digital products.',
      p1: 'Look at the tools you use every day. Most are clunky, slow, or just plain boring. The baseline for digital experiences has settled somewhere between "barely functional" and "forgettable."',
      p2: 'Companies bleed revenue not because their idea is flawed, but because their execution lacks soul. In a sea of templates, average is the most dangerous place a brand can be.',
      align: 'left'
    },
    {
      num: '02',
      title: 'We believe every company deserves a world-class digital presence.',
      p1: 'Your product is your absolute best salesperson. It doesn\'t sleep, it doesn\'t take days off. It should feel intuitive, look striking, and function flawlessly.',
      p2: 'We reject the compromise between aesthetic beauty and technical performance. The best digital products do both beautifully.',
      align: 'right'
    },
    {
      num: '03',
      title: 'So we built a studio that does it differently.',
      p1: 'No fluff. No bloated agency retainers. Just a ruthless focus on building what matters with the best craft possible.',
      p2: 'From deep strategic foundations to pixel-perfect execution, our process is designed to push your brand from where it is to where it simply must be.',
      align: 'center'
    }
  ]

  const Art01 = () => (
    <div className="relative w-full aspect-square md:aspect-[4/3] flex items-center justify-center pointer-events-none">
      <div className="absolute w-64 h-64 bg-signal mix-blend-difference rounded-full blur-2xl opacity-40 animate-pulse-slow"></div>
      <div className="absolute w-40 h-40 bg-ember rounded-tr-full rounded-bl-full rotate-45 transform mix-blend-overlay"></div>
      <div className="absolute w-48 h-48 bg-ink-600 rounded-sm clip-diagonal"></div>
      <div className="absolute inset-0 border-[1px] border-white/10" style={{ backgroundImage: 'linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px)', backgroundSize: '40px 40px'}}></div>
    </div>
  )

  const Art02 = () => (
    <div className="relative w-full aspect-square md:aspect-[4/3] flex items-center justify-center pointer-events-none group">
      <div className="w-1 h-3/4 bg-white/20 mx-4"></div>
      <div className="w-16 h-1/2 bg-signal/80 mx-4 transition-transform group-hover:scale-y-110"></div>
      <div className="w-1 h-2/3 bg-white/20 mx-4"></div>
      <div className="w-1 h-1/4 bg-white/10 mx-4"></div>
      <div className="w-8 h-8 bg-ember rounded-full mx-4 absolute right-1/4 top-1/4 animate-bounce"></div>
    </div>
  )

  return (
    <section className="bg-ink-950 py-32 md:py-48 relative overflow-hidden text-mist-100" id="about">
      <div className="max-w-7xl mx-auto px-6 md:px-12 flex flex-col pt-12">
        {chapters.map((chapter, i) => (
          <div key={i} className="mb-24 md:mb-48 relative last:mb-0">
            <motion.div
              initial={{ opacity: 0, y: 80 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: '-100px' }}
              transition={{ duration: 0.9, ease: [0.25, 0.46, 0.45, 0.94] }}
              className={`grid grid-cols-1 md:grid-cols-2 gap-16 md:gap-24 items-center ${chapter.align === 'right' ? 'md:flex-row-reverse' : ''} ${chapter.align === 'center' ? 'md:grid-cols-1 md:w-3/4 mx-auto text-center' : ''}`}
            >
              
              {/* Text Side */}
              <div className={`relative z-10 ${chapter.align === 'right' ? 'md:col-start-2 md:row-start-1' : ''}`}>
                <div className="absolute -top-16 md:-top-32 -left-8 md:-left-16 font-display text-[15rem] md:text-[20rem] text-white/[0.02] leading-none select-none pointer-events-none font-bold">
                  {chapter.num}
                </div>
                
                <h2 className="font-display text-4xl md:text-5xl lg:text-6xl font-medium tracking-tight leading-tight mb-8">
                  {chapter.title}
                </h2>
                
                <div className={`flex flex-col gap-6 text-mist-900 text-lg leading-relaxed ${chapter.align === 'center' ? 'items-center' : ''}`}>
                  <p>{chapter.p1}</p>
                  <p>{chapter.p2}</p>
                </div>

                {chapter.align === 'center' && (
                  <div className="mt-12">
                    <a href="#team" className="inline-flex items-center gap-2 font-mono text-sm uppercase tracking-widest text-signal hover:text-white transition-colors" data-cursor="hover">
                      Meet the team →
                    </a>
                  </div>
                )}
              </div>

              {/* Visual Side */}
              {chapter.align !== 'center' && (
                <div className={`relative z-0 ${chapter.align === 'right' ? 'md:col-start-1 md:row-start-1' : ''}`}>
                  {i === 0 ? <Art01 /> : <Art02 />}
                </div>
              )}

            </motion.div>

            {/* Chapter dividers */}
            {i < chapters.length - 1 && (
              <div className="my-24 md:my-48 relative flex justify-center items-center">
                <hr className="w-full border-white/5 absolute" />
                <span className="bg-ink-950 px-4 font-mono text-xs text-white/20 relative">Chapter {chapters[i+1].num}</span>
              </div>
            )}
          </div>
        ))}
      </div>
    </section>
  )
}
