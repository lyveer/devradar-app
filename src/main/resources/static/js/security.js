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

    // Anti-Debugging / Inspector Blocker
    setInterval(function() {
        const startTime = +new Date();
        debugger;
        const endTime = +new Date();
        if (endTime - startTime > 100) {
            // DevTools is likely open
        }
    }, 1000);
})();
