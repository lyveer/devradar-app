/**
 * DevRadarAI - Client-side Security & Anti-Inspection Protections
 * Designed by Lyver Software
 */
(function() {
    'use strict';

    // Disable Right-Click Context Menu
    document.addEventListener('contextmenu', function(e) {
        e.preventDefault();
    });

    // Disable Keyboard Shortcuts for DevTools / Page Source
    document.addEventListener('keydown', function(e) {
        // F12
        if (e.key === 'F12') {
            e.preventDefault();
            return false;
        }
        // Ctrl+Shift+I, Ctrl+Shift+J, Ctrl+Shift+C
        if (e.ctrlKey && e.shiftKey && (e.key === 'I' || e.key === 'i' || e.key === 'J' || e.key === 'j' || e.key === 'C' || e.key === 'c')) {
            e.preventDefault();
            return false;
        }
        // Ctrl+U (View Source)
        if (e.ctrlKey && (e.key === 'U' || e.key === 'u')) {
            e.preventDefault();
            return false;
        }
        // Ctrl+S (Save Page)
        if (e.ctrlKey && (e.key === 'S' || e.key === 's')) {
            e.preventDefault();
            return false;
        }
    });

    // Overwrite Console functions to prevent data leaking/reading
    const noop = function() {};
    console.log = noop;
    console.debug = noop;
    console.info = noop;
    console.warn = noop;
    console.error = noop;
    console.clear();

    // Anti-Debugging / Inspector Blocker
    setInterval(function() {
        const startTime = +new Date();
        debugger;
        const endTime = +new Date();
        if (endTime - startTime > 100) {
            // DevTools is open - blank out the content
            document.body.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100vh;color:white;font-family:sans-serif;font-size:1.5rem;background:#09090b;text-align:center;padding:2rem;">🛡️ Security Alert: Developer Tools detected. Access is restricted.</div>';
        }
    }, 1000);
})();
