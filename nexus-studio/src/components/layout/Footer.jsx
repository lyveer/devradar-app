import { Briefcase as Linkedin, MessageCircle as Twitter, Camera as Instagram, Palette as Dribbble } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="bg-ink-950 border-t border-white/5 pt-24 pb-8" id="contact">
      <div className="max-w-7xl mx-auto px-6 md:px-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 md:gap-8 mb-24">
          
          {/* Col 1 */}
          <div className="col-span-1 border-r-0 md:border-r md:border-white/5 pr-8">
            <div className="group inline-block mb-6 relative" data-cursor="hover">
              <span className="font-display text-3xl font-bold tracking-tight text-white group-hover:text-signal transition-colors duration-500">NEXUS</span>
              <div className="absolute top-1/2 left-full ml-4 opacity-0 group-hover:opacity-100 group-hover:translate-x-2 transition-all duration-300 text-mist-900 text-xs text-nowrap">
                Design & Engineering
              </div>
            </div>
            <p className="text-mist-900 text-sm mb-8 leading-relaxed max-w-xs">
              We don't build products.<br/>We build futures.
            </p>
            <div className="flex gap-4 text-mist-900">
              <a href="#" className="hover:text-white transition-colors p-2 -ml-2 rounded-full hover:bg-white/5"><Linkedin size={20} /></a>
              <a href="#" className="hover:text-white transition-colors p-2 rounded-full hover:bg-white/5"><Twitter size={20} /></a>
              <a href="#" className="hover:text-white transition-colors p-2 rounded-full hover:bg-white/5"><Instagram size={20} /></a>
              <a href="#" className="hover:text-white transition-colors p-2 rounded-full hover:bg-white/5"><Dribbble size={20} /></a>
            </div>
          </div>

          {/* Col 2 */}
          <div className="col-span-1">
            <h4 className="font-mono text-xs text-signal uppercase tracking-widest mb-6">Services</h4>
            <ul className="flex flex-col gap-4 text-sm text-mist-500">
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">Strategy & Consulting</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">UI/UX Design</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">Web Development</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">Mobile Apps</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">AI Integration</a></li>
            </ul>
          </div>

          {/* Col 3 */}
          <div className="col-span-1">
            <h4 className="font-mono text-xs text-signal uppercase tracking-widest mb-6">Company</h4>
            <ul className="flex flex-col gap-4 text-sm text-mist-500">
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">About Us</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">Our Process</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">The Team</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">Blog & Insights</a></li>
              <li><a href="#" className="hover:text-white transition-colors" data-cursor="text">Careers</a></li>
            </ul>
          </div>

          {/* Col 4 */}
          <div className="col-span-1">
            <h4 className="font-mono text-xs text-signal uppercase tracking-widest mb-6">Contact</h4>
            <div className="flex flex-col gap-4 text-sm text-mist-500">
              <a href="mailto:hello@nexus.studio" className="hover:text-white transition-colors" data-cursor="hover">hello@nexus.studio</a>
              <a href="tel:+919876543210" className="hover:text-white transition-colors" data-cursor="hover">+91 98765 43210</a>
              <p className="mt-4 text-mist-700 leading-relaxed">
                100ft Road, Indiranagar<br/>
                Bangalore, India 560038
              </p>
              <a href="#" className="text-signal hover:underline mt-2 inline-block" data-cursor="hover">View on Map →</a>
            </div>
          </div>

        </div>

        {/* Bottom Bar */}
        <div className="border-t border-white/5 pt-8 flex flex-col md:flex-row justify-between items-center gap-4 text-xs font-mono text-mist-900">
          <p>© <span title="Our founding year — and still going strong." className="cursor-help hover:text-signal transition-colors">2025</span> NEXUS Studio. All rights reserved.</p>
          <p>Designed with <span className="text-ember">♥</span> in Bangalore</p>
          <div className="flex gap-4">
            <a href="#" className="hover:text-white">Privacy Policy</a>
            <span>·</span>
            <a href="#" className="hover:text-white">Terms</a>
            <span>·</span>
            <a href="#" className="hover:text-white">Sitemap</a>
          </div>
        </div>
      </div>
    </footer>
  )
}
