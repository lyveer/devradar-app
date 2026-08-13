// API Base URL
const API_URL = '/api';

// Translation Helper
function t(key, fallback) {
    if (typeof getLanguage === 'function' && typeof translations !== 'undefined') {
        const lang = getLanguage();
        if (translations[lang] && translations[lang][key]) {
            return translations[lang][key];
        }
    }
    return fallback;
}

// Toast System
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✓' : '⚠️'}</span>
        <div>${message}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Global Auth State Helper
function getAuthHeaders() {
    const token = localStorage.getItem('devradar_token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

let currentVerificationEmail = '';

// Switch between Login and Register on auth.html
function switchAuthMode(mode) {
    const title = document.getElementById('auth-title');
    const desc = document.getElementById('auth-desc');
    
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const verifyForm = document.getElementById('verify-form');
    const forgotForm = document.getElementById('forgot-form');
    const resetForm = document.getElementById('reset-form');
    
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const tabsContainer = document.getElementById('auth-tabs-container');
    const socialDivider = document.getElementById('social-divider');
    const socialButtons = document.getElementById('social-buttons');

    if (!title) return;

    // Default forms hide
    if (loginForm) loginForm.style.display = 'none';
    if (registerForm) registerForm.style.display = 'none';
    if (verifyForm) verifyForm.style.display = 'none';
    if (forgotForm) forgotForm.style.display = 'none';
    if (resetForm) resetForm.style.display = 'none';

    if (mode === 'login') {
        title.setAttribute('data-i18n', 'auth-login-title');
        desc.setAttribute('data-i18n', 'auth-login-desc');
        if (loginForm) loginForm.style.display = 'block';
        if (tabLogin) tabLogin.classList.add('active');
        if (tabRegister) tabRegister.classList.remove('active');
        if (tabsContainer) tabsContainer.style.display = 'flex';
        if (socialDivider) socialDivider.style.display = 'flex';
        if (socialButtons) socialButtons.style.display = 'flex';
    } else if (mode === 'register') {
        title.setAttribute('data-i18n', 'auth-register-title');
        desc.setAttribute('data-i18n', 'auth-register-desc');
        if (registerForm) registerForm.style.display = 'block';
        if (tabLogin) tabLogin.classList.remove('active');
        if (tabRegister) tabRegister.classList.add('active');
        if (tabsContainer) tabsContainer.style.display = 'flex';
        if (socialDivider) socialDivider.style.display = 'flex';
        if (socialButtons) socialButtons.style.display = 'flex';
    } else if (mode === 'verify') {
        title.setAttribute('data-i18n', 'auth-verify-title-header');
        desc.setAttribute('data-i18n', 'auth-verify-desc');
        if (verifyForm) verifyForm.style.display = 'block';
        if (tabsContainer) tabsContainer.style.display = 'none';
        if (socialDivider) socialDivider.style.display = 'none';
        if (socialButtons) socialButtons.style.display = 'none';
    } else if (mode === 'forgot-password') {
        title.setAttribute('data-i18n', 'auth-forgot-title');
        desc.setAttribute('data-i18n', 'auth-forgot-desc');
        if (forgotForm) forgotForm.style.display = 'block';
        if (tabsContainer) tabsContainer.style.display = 'none';
        if (socialDivider) socialDivider.style.display = 'none';
        if (socialButtons) socialButtons.style.display = 'none';
    } else if (mode === 'reset-password') {
        title.setAttribute('data-i18n', 'auth-reset-title');
        desc.setAttribute('data-i18n', 'auth-reset-desc');
        if (resetForm) resetForm.style.display = 'block';
        if (tabsContainer) tabsContainer.style.display = 'none';
        if (socialDivider) socialDivider.style.display = 'none';
        if (socialButtons) socialButtons.style.display = 'none';
    }

    if (typeof applyTranslations === 'function') {
        applyTranslations();
    }
}

// Authentication Logic
async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn ? submitBtn.innerText : t('nav-login', 'Giriş Yap');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerText = t('toast-login-loading', 'Giriş yapılıyor...');
    }

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('devradar_token', data.token);
            localStorage.setItem('devradar_email', data.email);
            localStorage.setItem('devradar_name', data.fullName);
            showToast(t('toast-login-success', 'Giriş başarılı! Yönlendiriliyorsunuz...'), 'success');
            setTimeout(() => window.location.href = '/dashboard', 1000);
        } else {
            if (data.message && data.message.includes('EMAIL_NOT_VERIFIED')) {
                currentVerificationEmail = email;
                document.getElementById('verify-email-text').innerText = `${email} ${t('verify-email-text-sent', 'adresine doğrulama kodu gönderilmiştir. Lütfen kodu girin.')}`;
                showToast(t('toast-please-verify', 'Lütfen e-posta adresinizi doğrulayın.'), 'error');
                switchAuthMode('verify');
            } else {
                showToast(data.message || t('toast-login-fail', 'Giriş başarısız'), 'error');
            }
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerText = originalText;
            }
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerText = originalText;
        }
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const fullName = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn ? submitBtn.innerText : t('auth-register-btn', 'Kayıt Ol');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerText = t('toast-register-loading', 'Kayıt yapılıyor...');
    }

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            currentVerificationEmail = email;
            document.getElementById('verify-email-text').innerText = `${email} ${t('verify-email-text-sent', 'adresine doğrulama kodu gönderilmiştir. Lütfen kodu girin.')}`;
            showToast(t('toast-register-success', 'Kayıt başarılı! Lütfen e-postanıza gelen doğrulama kodunu girin.'), 'success');
            switchAuthMode('verify');
        } else {
            showToast(data.message || t('toast-register-fail', 'Kayıt başarısız'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerText = originalText;
        }
    }
}

async function handleVerify(event) {
    event.preventDefault();
    const code = document.getElementById('verify-code').value;
    const email = currentVerificationEmail;

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn ? submitBtn.innerText : t('btn-verify-login', 'Doğrula ve Giriş Yap');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerText = t('toast-verify-loading', 'Doğrulanıyor...');
    }

    try {
        const response = await fetch(`${API_URL}/auth/verify`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, code })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('devradar_token', data.token);
            localStorage.setItem('devradar_email', data.email);
            localStorage.setItem('devradar_name', data.fullName);
            showToast(t('toast-verify-success', 'Hesabınız başarıyla doğrulandı! Yönlendiriliyorsunuz...'), 'success');
            setTimeout(() => window.location.href = '/dashboard', 1000);
        } else {
            showToast(data.message || t('toast-verify-fail', 'Doğrulama başarısız'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerText = originalText;
        }
    }
}

let isResendingCode = false;
async function handleResendCode() {
    if (isResendingCode) return;
    const email = currentVerificationEmail;
    if (!email) {
        showToast(t('toast-email-not-found', 'E-posta adresi bulunamadı'), 'error');
        return;
    }

    isResendingCode = true;
    const resendBtn = document.getElementById('resend-code-btn');
    const originalText = resendBtn ? resendBtn.innerText : 'Kodu Tekrar Gönder';
    if (resendBtn) {
        resendBtn.innerText = 'Gönderiliyor...';
        resendBtn.style.pointerEvents = 'none';
        resendBtn.style.opacity = '0.5';
    }

    try {
        const response = await fetch(`${API_URL}/auth/resend?email=${encodeURIComponent(email)}`, {
            method: 'POST'
        });

        if (response.ok) {
            showToast(t('toast-code-resent', 'Yeni doğrulama kodu e-postanıza gönderildi!'), 'success');
            let cooldown = 30;
            const interval = setInterval(() => {
                cooldown--;
                if (cooldown <= 0) {
                    clearInterval(interval);
                    isResendingCode = false;
                    if (resendBtn) {
                        resendBtn.innerText = originalText;
                        resendBtn.style.pointerEvents = 'auto';
                        resendBtn.style.opacity = '1';
                    }
                } else {
                    if (resendBtn) {
                        resendBtn.innerText = `${cooldown}s`;
                    }
                }
            }, 1000);
        } else {
            showToast(t('toast-code-send-fail', 'Kod gönderilemedi'), 'error');
            isResendingCode = false;
            if (resendBtn) {
                resendBtn.innerText = originalText;
                resendBtn.style.pointerEvents = 'auto';
                resendBtn.style.opacity = '1';
            }
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        isResendingCode = false;
        if (resendBtn) {
            resendBtn.innerText = originalText;
            resendBtn.style.pointerEvents = 'auto';
            resendBtn.style.opacity = '1';
        }
    }
}

async function handleForgotPassword(event) {
    event.preventDefault();
    const email = document.getElementById('forgot-email').value;

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn ? submitBtn.innerText : t('btn-resend-btn', 'Sıfırlama Kodu Gönder');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerText = t('btn-resending', 'Gönderiliyor...');
    }

    try {
        const response = await fetch(`${API_URL}/auth/forgot-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        if (response.ok) {
            currentVerificationEmail = email;
            document.getElementById('reset-email-text').innerText = `${email} ${t('reset-email-text-sent', 'adresine gönderilen 6 haneli şifre sıfırlama kodunu ve yeni şifrenizi girin.')}`;
            showToast(t('toast-forgot-sent', 'Sıfırlama kodu e-postanıza gönderildi!'), 'success');
            switchAuthMode('reset-password');
        } else {
            const data = await response.json().catch(() => ({}));
            showToast(data.message || t('toast-forgot-fail', 'Sıfırlama kodu gönderilemedi'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerText = originalText;
        }
    }
}

async function handleResetPassword(event) {
    event.preventDefault();
    const email = currentVerificationEmail;
    const code = document.getElementById('reset-code').value;
    const newPassword = document.getElementById('reset-password').value;

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn ? submitBtn.innerText : t('auth-reset-btn', 'Şifremi Güncelle');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerText = t('toast-reset-loading', 'Güncelleniyor...');
    }

    try {
        const response = await fetch(`${API_URL}/auth/reset-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, code, newPassword })
        });

        if (response.ok) {
            showToast(t('toast-reset-success', 'Şifreniz başarıyla sıfırlandı! Yeni şifrenizle giriş yapabilirsiniz.'), 'success');
            switchAuthMode('login');
        } else {
            const data = await response.json().catch(() => ({}));
            showToast(data.message || t('toast-reset-fail', 'Şifre sıfırlama başarısız'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerText = originalText;
        }
    }
}

function handleLogout() {
    localStorage.removeItem('devradar_token');
    localStorage.removeItem('devradar_email');
    localStorage.removeItem('devradar_name');
    window.location.href = '/auth?mode=login';
}

// Sidebar Menu Navigation
function switchTab(tabId) {
    // Buttons
    document.querySelectorAll('.sidebar-link').forEach(link => link.classList.remove('active'));
    const btn = document.getElementById(`menu-${tabId}`);
    if (btn) btn.classList.add('active');

    // Panes
    document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));
    const pane = document.getElementById(`tab-${tabId}-pane`);
    if (pane) pane.classList.add('active');

    if (tabId === 'history') {
        loadHistoryList();
    } else if (tabId === 'progress') {
        loadProgressTracker();
    } else if (tabId === 'announcements') {
        loadAnnouncements();
    } else if (tabId === 'admin') {
        loadAdminPanel();
    } else if (tabId === 'settings') {
        loadSettingsForm();
    } else if (tabId === 'agent') {
        loadUserInfo();
        loadAgentProjectContexts();
    }
}

// FAQ Accordion Toggle
function toggleFaq(element) {
    const parent = element.parentElement;
    parent.classList.toggle('active');
}

// Load user credits and subscription status
async function loadUserInfo() {
    try {
        const response = await fetch(`${API_URL}/auth/me`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const user = await response.json();
            const creditDisplay = document.getElementById('credit-display');
            const subscribeBtn = document.getElementById('subscribe-btn');

            // Show admin panel menu if email contains "admin"
            const isAdmin = user.email.toLowerCase().includes('admin');
            const adminMenu = document.getElementById('menu-admin');
            if (adminMenu) {
                adminMenu.style.display = isAdmin ? 'block' : 'none';
            }

            if (creditDisplay) {
                const creditsLabel = t('nav-credits', 'Kredi:');
                const unlimitedText = t('unlimited-text', 'Sınırsız');
                if (user.isPremium) {
                    creditDisplay.innerText = `${creditsLabel} ${unlimitedText}`;
                    creditDisplay.style.background = 'rgba(124, 58, 237, 0.15)';
                    creditDisplay.style.color = 'var(--primary-light)';
                    creditDisplay.style.borderColor = 'rgba(124, 58, 237, 0.25)';
                    if (subscribeBtn) subscribeBtn.style.display = 'none';
                } else {
                    creditDisplay.innerText = `${creditsLabel} ${user.credits}`;
                    creditDisplay.style.background = 'rgba(16, 185, 129, 0.15)';
                    creditDisplay.style.color = '#10b981';
                    creditDisplay.style.borderColor = 'rgba(16, 185, 129, 0.25)';
                    if (subscribeBtn) subscribeBtn.style.display = 'inline-flex';
                }
            }

            // Show AI provider selector for premium users only
            const aiProviderGroup = document.getElementById('ai-provider-group');
            if (aiProviderGroup) {
                aiProviderGroup.style.display = user.isPremium ? 'block' : 'none';
            }

            // Populate AI assistant provider selection based on premium tier
            const agentProvider = document.getElementById('agent-provider');
            if (agentProvider) {
                if (user.isPremium) {
                    agentProvider.innerHTML = `
                        <option value="gemini">✨ Gemini</option>
                        <option value="claude">✨ Claude</option>
                        <option value="chatgpt">✨ ChatGPT (OpenAI)</option>
                        <option value="groq">⚡ Groq (Hızlı)</option>
                    `;
                } else {
                    agentProvider.innerHTML = `
                        <option value="groq">Groq (Llama-3)</option>
                    `;
                }
            }

            // Update remaining credits in Code Assistant Workspace
            const agentCreditsInfo = document.getElementById('agent-credits-info');
            if (agentCreditsInfo) {
                agentCreditsInfo.innerText = user.isPremium 
                    ? t('unlimited-credits-agent', 'Sınırsız Kredi') 
                    : `${t('remaining-credits-agent', 'Kalan Kredi')}: ${user.credits}`;
            }

            // Update user profile card in settings/profile tab if elements exist
            const cardName = document.getElementById('profile-card-name');
            const cardEmail = document.getElementById('profile-card-email');
            const cardCredits = document.getElementById('profile-card-credits');
            const cardDate = document.getElementById('profile-card-date');
            const cardAvatar = document.getElementById('profile-card-avatar');
            const cardBadge = document.getElementById('profile-card-badge');

            if (cardName) cardName.innerText = user.fullName || 'Geliştirici';
            if (cardEmail) cardEmail.innerText = user.email || '';
            if (cardCredits) cardCredits.innerText = user.isPremium ? t('unlimited-text', 'Sınırsız') : user.credits;
            if (cardDate) {
                if (user.createdAt) {
                    const d = new Date(user.createdAt);
                    cardDate.innerText = d.toLocaleDateString(t('locale-date-format', 'tr-TR'), { day: '2-digit', month: '2-digit', year: 'numeric' });
                } else {
                    cardDate.innerText = '-';
                }
            }
            if (cardAvatar) {
                const name = user.fullName || '?';
                const parts = name.trim().split(' ');
                let initials = '?';
                if (parts.length === 1) initials = parts[0].substring(0, 2).toUpperCase();
                else if (parts.length > 1) initials = (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
                cardAvatar.innerText = initials;
            }
            if (cardBadge) {
                if (user.isPremium) {
                    cardBadge.innerText = t('settings-premium-member', 'PREMIUM ÜYE');
                    cardBadge.className = 'badge badge-premium';
                } else {
                    cardBadge.innerText = t('settings-member-type', 'STANDART ÜYE');
                    cardBadge.className = 'badge badge-standard';
                }
            }
        }
    } catch (err) {
        console.error('Kullanıcı bilgileri yüklenemedi', err);
    }
}

// Utility: Escape HTML for safe rendering inside <pre>
function escapeHtml(text) {
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Utility: Copy snippet to clipboard
function copySnippet(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.select();
    try {
        document.execCommand('copy');
        showToast('Kod kopyalandı!', 'success');
    } catch (e) {
        navigator.clipboard.writeText(el.value).then(() => showToast('Kod kopyalandı!', 'success'));
    }
}

// Handle Subscription
async function handleSubscribe() {
    try {
        const response = await fetch(`${API_URL}/auth/subscribe`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            showToast(t('toast-sub-success', 'Abonelik başarıyla tamamlandı! Artık sınırsız analiz yapabilirsiniz.'), 'success');
            await loadUserInfo();
        } else {
            showToast(t('toast-sub-fail', 'Abonelik işlemi gerçekleştirilemedi.'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Bağlantı hatası'), 'error');
    }
}

// Specialization Card Selection
function selectSpecialization(value) {
    const input = document.getElementById('profile-specialization');
    if (input) {
        input.value = value;
    }
    document.querySelectorAll('.spec-card').forEach(card => {
        if (card.getAttribute('data-value') === value) {
            card.classList.add('selected');
        } else {
            card.classList.remove('selected');
        }
    });
}

// Profile Page Operations
async function loadProfile() {
    try {
        const response = await fetch(`${API_URL}/profile`, {
            headers: getAuthHeaders()
        });

        if (response.status === 403) {
            handleLogout();
            return;
        }

        if (response.ok) {
            const profile = await response.json();
            if (profile) {
                // Populate profile fields
                selectSpecialization(profile.specialization || '');
                document.getElementById('profile-experience').value = profile.experienceYears || '0';
                document.getElementById('profile-projects').value = profile.previousProjects || '';
                document.getElementById('profile-github').value = profile.githubUrl || '';

                // Select language checkboxes
                if (profile.languages) {
                    const langs = JSON.parse(profile.languages);
                    langs.forEach(lang => {
                        const cb = Array.from(document.querySelectorAll('input[name="languages"]'))
                                        .find(c => c.value === lang);
                        if (cb) cb.checked = true;
                    });
                }

                // AI evaluation display
                if (profile.aiScore !== null && profile.aiScore !== undefined) {
                    renderProfileScore(profile.aiScore, profile.aiSummary, 
                        JSON.parse(profile.aiStrengths || '[]'),
                        JSON.parse(profile.aiWeaknesses || '[]'),
                        JSON.parse(profile.aiRecommendations || '[]'));
                } else {
                    document.getElementById('score-btn').style.display = 'inline-flex';
                    document.getElementById('profile-ai-empty').style.display = 'block';
                    document.getElementById('profile-ai-results').style.display = 'none';
                }
            } else {
                selectSpecialization('');
                document.getElementById('profile-ai-empty').style.display = 'block';
            }
        }
    } catch (err) {
        showToast(t('toast-profile-load-fail', 'Profil bilgileri yüklenemedi'), 'error');
    }
}

async function handleSaveProfile(event) {
    event.preventDefault();

    const specialization = document.getElementById('profile-specialization').value;
    const experienceYears = parseInt(document.getElementById('profile-experience').value);
    const previousProjects = document.getElementById('profile-projects').value;
    const githubUrl = document.getElementById('profile-github').value;

    const languages = Array.from(document.querySelectorAll('input[name="languages"]:checked'))
                           .map(cb => cb.value);

    if (languages.length === 0) {
        showToast(t('toast-select-lang-error', 'Lütfen en az bir programlama dili seçin'), 'error');
        return;
    }

    const body = { specialization, experienceYears, previousProjects, githubUrl, languages };

    try {
        const response = await fetch(`${API_URL}/profile`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(body)
        });

        if (response.ok) {
            const profile = await response.json();
            showToast(t('toast-profile-saved', 'Profil başarıyla güncellendi ve Yapay Zeka ile analiz edildi!'), 'success');
            await loadUserInfo(); // Update remaining credits display
            if (profile.aiScore !== null && profile.aiScore !== undefined) {
                renderProfileScore(profile.aiScore, profile.aiSummary, 
                    JSON.parse(profile.aiStrengths || '[]'),
                    JSON.parse(profile.aiWeaknesses || '[]'),
                    JSON.parse(profile.aiRecommendations || '[]'));
            } else {
                document.getElementById('score-btn').style.display = 'inline-flex';
                document.getElementById('profile-ai-empty').style.display = 'block';
                document.getElementById('profile-ai-results').style.display = 'none';
            }
        } else {
            showToast(t('toast-profile-fail', 'Profil kaydedilemedi'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Bağlantı hatası'), 'error');
    }
}

async function handleTriggerScoring() {
    const scoreBtn = document.getElementById('score-btn');
    scoreBtn.innerText = t('toast-verify-loading', 'Analiz Ediliyor...');
    scoreBtn.disabled = true;

    try {
        const response = await fetch(`${API_URL}/profile/score`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            renderProfileScore(data.score, data.summary, data.strengths, data.weaknesses, data.recommendations);
            showToast(t('toast-ai-scoring-complete', 'Profil AI Analizi tamamlandı!'), 'success');
            await loadUserInfo(); // Update remaining credits display
        } else {
            const errData = await response.json().catch(() => ({}));
            showToast(errData.message || t('toast-profile-fail', 'AI Puanlama başarısız oldu'), 'error');
            scoreBtn.innerText = t('profile-score-btn', 'Profil Puanımı Analiz Et');
            scoreBtn.disabled = false;
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Bağlantı hatası'), 'error');
        scoreBtn.innerText = t('profile-score-btn', 'Profil Puanımı Analiz Et');
        scoreBtn.disabled = false;
    }
}

function renderProfileScore(score, summary, strengths, weaknesses, recommendations) {
    document.getElementById('profile-ai-empty').style.display = 'none';
    document.getElementById('score-btn').style.display = 'none';
    
    const results = document.getElementById('profile-ai-results');
    results.style.display = 'block';

    // Update circular gauge meter
    const meter = document.getElementById('profile-score-meter');
    const val = document.getElementById('profile-score-val');
    val.innerText = score;
    
    // Scale conic gradient angle based on score percentage
    const angle = (score / 100) * 360;
    meter.style.setProperty('--progress', `${angle}deg`);

    // Color progress indicator based on value
    if (score >= 80) {
        meter.style.background = `conic-gradient(var(--success) ${angle}deg, rgba(255,255,255,0.05) 0deg)`;
    } else if (score >= 50) {
        meter.style.background = `conic-gradient(var(--warning) ${angle}deg, rgba(255,255,255,0.05) 0deg)`;
    } else {
        meter.style.background = `conic-gradient(var(--accent) ${angle}deg, rgba(255,255,255,0.05) 0deg)`;
    }

    // Populate lists
    document.getElementById('profile-ai-summary').innerText = summary;
    
    const strengthEl = document.getElementById('profile-ai-strengths');
    strengthEl.innerHTML = strengths.map(s => `<li>${s}</li>`).join('');

    const weaknessEl = document.getElementById('profile-ai-weaknesses');
    weaknessEl.innerHTML = weaknesses.map(w => `<li>${w}</li>`).join('');

    const recEl = document.getElementById('profile-ai-recommendations');
    recEl.innerHTML = recommendations.map(r => `<li>${r}</li>`).join('');
}

// Project Analysis Tab Operations
async function handleAnalyzeProject(event) {
    event.preventDefault();

    const projectName = document.getElementById('project-name').value;
    const projectDescription = document.getElementById('project-description').value;
    const targetLanguage = document.getElementById('project-language').value;
    const aiProviderEl = document.getElementById('project-ai-provider');
    const aiProvider = aiProviderEl ? aiProviderEl.value : null;

    const submitBtn = document.getElementById('analyze-submit-btn');
    submitBtn.disabled = true;
    submitBtn.innerText = 'Yapay Zeka Analiz Ediyor...';

    document.getElementById('analysis-empty').style.display = 'none';
    document.getElementById('analysis-loading').style.display = 'block';
    document.getElementById('analysis-content').style.display = 'none';

    try {
        const body = { projectName, projectDescription, targetLanguage };
        if (aiProvider) body.aiProvider = aiProvider;

        const response = await fetch(`${API_URL}/analysis`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(body)
        });

        if (response.ok) {
            const data = await response.json();
            renderProjectAnalysis(data);
            showToast('Proje analizi başarıyla oluşturuldu!', 'success');
            await loadUserInfo(); // Update remaining credits
        } else {
            const errData = await response.json().catch(() => ({}));
            showToast(errData.message || 'Analiz başlatılamadı', 'error');
            resetAnalysisPlaceholder();
        }
    } catch (err) {
        showToast('Bağlantı hatası oluştu', 'error');
        resetAnalysisPlaceholder();
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = 'Analizi Başlat';
    }
}

function resetAnalysisPlaceholder() {
    document.getElementById('analysis-empty').style.display = 'block';
    document.getElementById('analysis-loading').style.display = 'none';
    document.getElementById('analysis-content').style.display = 'none';
}

function renderProjectAnalysis(data) {
    document.getElementById('analysis-empty').style.display = 'none';
    document.getElementById('analysis-loading').style.display = 'none';
    
    const content = document.getElementById('analysis-content');
    content.style.display = 'block';

    // Fallbacks for older or partial analysis records missing new fields
    if (!data.marketPriceRange) {
        data.marketPriceRange = { min: 5000, max: 20000, currency: 'USD' };
    }
    if (!data.freelancerIncome) {
        data.freelancerIncome = { hourlyRate: { min: 25, max: 75 }, projectBased: { min: 3000, max: 12000 } };
    } else if (!data.freelancerIncome.hourlyRate) {
        data.freelancerIncome.hourlyRate = { min: 25, max: 75 };
    }
    if (!data.estimatedDevelopmentTime) {
        data.estimatedDevelopmentTime = { minWeeks: 4, maxWeeks: 12, description: 'Temel MVP geliştirme süresi' };
    }
    if (!data.recommendedTechStack) {
        data.recommendedTechStack = [];
    }
    if (!data.enhancements) {
        data.enhancements = [];
    }
    if (!data.tips) {
        data.tips = [];
    }
    if (!data.codeRecommendation) {
        data.codeRecommendation = `### Önerilen Proje Mimarisi (${data.targetLanguage || 'Teknoloji'})\n\n\`\`\`\nsrc/\n├── config/\n├── controllers/\n├── models/\n└── services/\n\`\`\`\n\n*(Not: Bu analiz eski bir sürümde yapıldığı için kod önerileri otomatik oluşturulmuştur. Güncel AI modeli ile yeni bir analiz başlatarak canlı ve detaylı öneriler alabilirsiniz.)*`;
    }
    if (!data.projectResources || data.projectResources.length === 0) {
        data.projectResources = [
            {
                title: `${data.targetLanguage || 'Teknoloji'} Resmi Dokümantasyonu`,
                url: `https://www.google.com/search?q=${encodeURIComponent((data.targetLanguage || '') + ' official documentation')}`
            },
            {
                title: "DevRadar AI Geliştirici Portalı",
                url: "https://github.com/lyveer/devradar-app"
            }
        ];
    }
    if (!data.codeSnippets || data.codeSnippets.length === 0) {
        data.codeSnippets = [
            {
                title: "Ana Uygulama İskeleti",
                code: `// ${data.targetLanguage || 'Teknoloji'} için başlangıç şablonu\n// Detaylı analiz için lütfen yeni bir analiz başlatın.\n\nconsole.log("${data.projectName || 'Proje'} başlatılıyor...");`,
                language: (data.targetLanguage || 'javascript').toLowerCase().split(' ')[0]
            }
        ];
    }

    document.getElementById('analysis-result-header').innerText = `${data.projectName} — Analiz Sonucu`;

    // Pricing
    const p = data.marketPriceRange;
    document.getElementById('res-market-price').innerText = `${p.min.toLocaleString()} - ${p.max.toLocaleString()} ${p.currency}`;

    // Freelancer
    const fl = data.freelancerIncome.hourlyRate;
    document.getElementById('res-freelancer-hourly').innerText = `$${fl.min}-$${fl.max}/saat`;

    // Demand
    const demandVal = document.getElementById('res-demand');
    demandVal.innerText = data.demandLevel;
    demandVal.className = 'metric-val';
    if (data.demandLevel === 'YÜKSEK' || data.demandLevel === 'ÇOK_YÜKSEK' || data.demandLevel === 'HIGH' || data.demandLevel === 'VERY_HIGH') {
        demandVal.classList.add('high');
    } else if (data.demandLevel === 'ORTA' || data.demandLevel === 'MEDIUM') {
        demandVal.classList.add('medium');
    } else {
        demandVal.classList.add('low');
    }
    document.getElementById('res-demand-desc').innerText = data.demandDescription;

    // Time
    const dt = data.estimatedDevelopmentTime;
    document.getElementById('res-dev-time').innerText = `${dt.minWeeks}-${dt.maxWeeks} Hafta`;
    document.getElementById('res-dev-time-desc').innerText = dt.description;

    // Tech recommendations
    const techEl = document.getElementById('res-tech-stack');
    techEl.innerHTML = data.recommendedTechStack.map(tech => `
        <div class="tech-tag-info" style="margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.75rem;">
            <span class="tech-tag" style="background: rgba(124, 58, 237, 0.15); color: var(--primary-light); font-weight: 600; padding: 4px 8px; border-radius: 4px; font-size: 0.85rem;">${tech.name}</span>
            <span class="score-desc" style="display: inline; font-size: 0.9rem; color: var(--text-secondary);">${tech.purpose}</span>
        </div>
    `).join('');

    // Enhancements
    const enhEl = document.getElementById('res-enhancements');
    enhEl.innerHTML = data.enhancements.map(enh => `
        <li style="margin-bottom: 0.5rem; color: var(--text-secondary);">
            <strong style="color: white;">${enh.title}:</strong>
            <span>${enh.description}</span>
        </li>
    `).join('');

    // Tips
    const tipEl = document.getElementById('res-tips');
    tipEl.innerHTML = data.tips.map(tip => `
        <li style="margin-bottom: 0.5rem; color: var(--text-secondary);">
            <strong style="color: white;">${tip.title}:</strong>
            <span>${tip.description}</span>
        </li>
    `).join('');

    // Competitor
    const compBox = document.getElementById('res-competitor-box');
    if (data.competitorInsight) {
        compBox.style.display = 'block';
        document.getElementById('res-competitor-insight').innerText = data.competitorInsight;
    } else {
        compBox.style.display = 'none';
    }

    // Competitors links list
    const competitorsListBox = document.getElementById('res-competitors-list-box');
    const competitorsList = document.getElementById('res-competitors-list');
    if (data.competitors && data.competitors.length > 0) {
        competitorsListBox.style.display = 'block';
        competitorsList.innerHTML = data.competitors.map(comp => `
            <span class="tech-tag" style="background: rgba(6, 182, 212, 0.15); color: var(--secondary); display: inline-flex; align-items: center; font-weight: 500; font-size: 0.9rem; padding: 0.4rem 0.8rem; border-radius: 6px; border: 1px solid rgba(6, 182, 212, 0.25);">
                ${comp.name}
            </span>
        `).join('');
    } else {
        competitorsListBox.style.display = 'none';
    }

    // Freelancer platforms and pricing links list
    const freelancerPlatformsBox = document.getElementById('res-freelancer-platforms-box');
    const freelancerPlatforms = document.getElementById('res-freelancer-platforms');
    if (data.freelancerPlatforms && data.freelancerPlatforms.length > 0) {
        freelancerPlatformsBox.style.display = 'block';
        freelancerPlatforms.innerHTML = data.freelancerPlatforms.map(plat => `
            <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.02); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--border-color); flex-wrap: wrap; gap: 0.5rem; width: 100%;">
                <div>
                    <strong style="color: var(--text-primary);">${plat.name}</strong>
                    <span style="color: var(--text-muted); font-size: 0.85rem; margin-left: 0.5rem;">tahmini kazanç</span>
                </div>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <span style="color: var(--success); font-weight: 600;">${plat.estimatedPrice}</span>
                </div>
            </div>
        `).join('');
    } else {
        freelancerPlatformsBox.style.display = 'none';
    }

    // Code Recommendation
    const codeRecBox = document.getElementById('res-code-rec-box');
    const codeRec = document.getElementById('res-code-rec');
    if (codeRecBox && codeRec) {
        if (data.codeRecommendation) {
            codeRecBox.style.display = 'block';
            codeRec.innerText = data.codeRecommendation;
        } else {
            codeRecBox.style.display = 'none';
        }
    }

    // Project Resources
    const resourcesBox = document.getElementById('res-resources-box');
    const resourcesList = document.getElementById('res-resources-list');
    if (resourcesBox && resourcesList) {
        if (data.projectResources && data.projectResources.length > 0) {
            resourcesBox.style.display = 'block';
            resourcesList.innerHTML = data.projectResources.map(r => `
                <a href="${r.url}" target="_blank" rel="noopener noreferrer"
                   style="display:flex;align-items:center;gap:0.6rem;padding:0.65rem 0.9rem;border-radius:8px;background:rgba(232,255,71,0.06);border:1px solid rgba(232,255,71,0.15);text-decoration:none;transition:background 0.2s;">
                    <span style="font-size:1rem;">&#128279;</span>
                    <span style="color:#e8ff47;font-size:0.82rem;font-weight:600;">${r.title}</span>
                    <span style="margin-left:auto;color:#9898b8;font-size:0.72rem;">${(r.url||'').replace(/^https?:\/\//, '').split('/')[0]}</span>
                </a>
            `).join('');
        } else {
            resourcesBox.style.display = 'none';
        }
    }

    // Code Snippets
    const snippetsBox = document.getElementById('res-snippets-box');
    const snippetsList = document.getElementById('res-snippets-list');
    if (snippetsBox && snippetsList) {
        if (data.codeSnippets && data.codeSnippets.length > 0) {
            snippetsBox.style.display = 'block';
            snippetsList.innerHTML = data.codeSnippets.map((snip, i) => `
                <div style="border:1px solid rgba(168,85,247,0.2);border-radius:10px;overflow:hidden;">
                    <div style="display:flex;align-items:center;justify-content:space-between;padding:0.6rem 0.9rem;background:rgba(168,85,247,0.1);border-bottom:1px solid rgba(168,85,247,0.15);">
                        <span style="color:#d8b4fe;font-size:0.8rem;font-weight:700;">${snip.title || 'Code Snippet'}</span>
                        <span style="color:#9898b8;font-size:0.7rem;font-family:monospace;text-transform:uppercase;">${snip.language || ''}</span>
                    </div>
                    <pre style="margin:0;padding:0.85rem 1rem;background:rgba(4,4,10,0.7);color:#e8e8f8;font-size:0.75rem;font-family:'JetBrains Mono',monospace;overflow-x:auto;white-space:pre;line-height:1.6;">${escapeHtml(snip.code || '')}</pre>
                    <div style="padding:0.4rem 0.9rem;background:rgba(168,85,247,0.05);">
                        <button onclick="copySnippet('snippet-${i}')" style="color:#9898b8;font-size:0.7rem;background:none;border:none;cursor:pointer;padding:0;">📋 Kopyala</button>
                        <textarea id="snippet-${i}" style="position:absolute;opacity:0;pointer-events:none;" readonly>${snip.code || ''}</textarea>
                    </div>
                </div>
            `).join('');
        } else {
            snippetsBox.style.display = 'none';
        }
    }
}

// History List Loading
async function loadHistoryList() {
    const listEl = document.getElementById('history-list');
    const emptyEl = document.getElementById('history-empty');

    try {
        const response = await fetch(`${API_URL}/analysis/history`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const list = await response.json();
            if (list.length > 0) {
                emptyEl.style.display = 'none';
                listEl.innerHTML = list.map(item => `
                    <div class="card" style="padding: 1.5rem; cursor: pointer; transition: all 0.2s;" onclick="viewHistoryItem(${JSON.stringify(item).replace(/"/g, '&quot;')})">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <h4 style="color: var(--text-primary); font-size: 1.1rem; margin: 0;">${item.projectName}</h4>
                                <span class="tech-tag" style="margin-top: 0.5rem; display: inline-block;">${item.targetLanguage}</span>
                            </div>
                            <div style="text-align: right;">
                                <span class="badge" style="margin-bottom: 0; padding: 0.25rem 0.75rem;">Talep: ${item.demandLevel}</span>
                                <p class="score-desc" style="font-size: 0.8rem; margin: 0.5rem 0 0 0;">${new Date(item.createdAt).toLocaleDateString('tr-TR')}</p>
                            </div>
                        </div>
                    </div>
                `).join('');
            } else {
                emptyEl.style.display = 'block';
                listEl.innerHTML = '';
            }
        }
    } catch (err) {
        showToast('Geçmiş analizler yüklenemedi', 'error');
    }
}

function viewHistoryItem(item) {
    switchTab('project');
    renderProjectAnalysis(item);
    
    // Fill the analysis input form with clicked history details
    document.getElementById('project-name').value = item.projectName;
    document.getElementById('project-description').value = item.projectDescription || '';
    document.getElementById('project-language').value = item.targetLanguage;
}

// --- NEW COMPONENT: Progress Tracker ---

async function loadProgressTracker() {
    const listEl = document.getElementById('progress-list');
    const emptyEl = document.getElementById('progress-empty');

    try {
        const response = await fetch(`${API_URL}/analysis/history`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const list = await response.json();
            if (list.length > 0) {
                emptyEl.style.display = 'none';
                
                listEl.innerHTML = list.map(item => {
                    // Check completion checks from DB, fall back to local storage
                    let savedChecks = [];
                    if (item.completedSteps !== undefined && item.completedSteps !== null) {
                        savedChecks = item.completedSteps ? item.completedSteps.split(',').map(x => parseInt(x)) : [];
                    } else {
                        const storageKey = `progress_${localStorage.getItem('devradar_email')}_${item.id}`;
                        savedChecks = JSON.parse(localStorage.getItem(storageKey) || '[]');
                    }
                    
                    const steps = [
                        "Frontend Tasarımı & Arayüz Şablonu",
                        "Veritabanı Şeması & API Tasarımı",
                        "Core Kod Entegrasyonu & İş Mantığı",
                        "Test & Hata Ayıklama (Debug)",
                        "Deployment & Canlıya Alma"
                    ];

                    let completedCount = 0;
                    const itemsHtml = steps.map((step, idx) => {
                        const isChecked = savedChecks.includes(idx);
                        if (isChecked) completedCount++;
                        return `
                            <label class="checklist-item ${isChecked ? 'completed' : ''}">
                                <input type="checkbox" onchange="toggleChecklistItem(${item.id}, ${idx}, this)" ${isChecked ? 'checked' : ''}>
                                <span>${step}</span>
                            </label>
                        `;
                    }).join('');

                    const percentage = Math.round((completedCount / steps.length) * 100);

                    return `
                        <div class="progress-list-item">
                            <div class="progress-header">
                                <h4>${item.projectName}</h4>
                                <span class="badge" style="margin-bottom: 0;">${percentage}% Tamamlandı</span>
                            </div>
                            <div class="progress-bar-container">
                                <div class="progress-bar-fill" id="bar-${item.id}" style="width: ${percentage}%"></div>
                            </div>
                            <div class="checklist-items">
                                ${itemsHtml}
                            </div>
                        </div>
                    `;
                }).join('');
            } else {
                emptyEl.style.display = 'block';
                listEl.innerHTML = '';
            }
        }
    } catch (err) {
        showToast('İlerleme verileri yüklenemedi', 'error');
    }
}

async function toggleChecklistItem(projectId, stepIdx, checkbox) {
    const userEmail = localStorage.getItem('devradar_email');
    const storageKey = `progress_${userEmail}_${projectId}`;
    let savedChecks = JSON.parse(localStorage.getItem(storageKey) || '[]');

    if (checkbox.checked) {
        if (!savedChecks.includes(stepIdx)) {
            savedChecks.push(stepIdx);
        }
        checkbox.parentElement.classList.add('completed');
    } else {
        savedChecks = savedChecks.filter(x => x !== stepIdx);
        checkbox.parentElement.classList.remove('completed');
    }

    localStorage.setItem(storageKey, JSON.stringify(savedChecks));

    // Update progress bar
    const totalSteps = 5;
    const percentage = Math.round((savedChecks.length / totalSteps) * 100);
    
    // Update label badge in header
    const container = checkbox.closest('.progress-list-item');
    const badge = container.querySelector('.progress-header .badge');
    badge.innerText = `${percentage}% Tamamlandı`;

    const fillBar = document.getElementById(`bar-${projectId}`);
    if (fillBar) fillBar.style.width = `${percentage}%`;

    // Save to database
    try {
        await fetch(`${API_URL}/analysis/${projectId}/steps`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(savedChecks)
        });
    } catch (err) {
        console.error('İlerleme sunucuya kaydedilemedi', err);
    }
}

// --- NEW COMPONENT: Announcements ---

async function loadAnnouncements() {
    const listEl = document.getElementById('announcements-list');
    const emptyEl = document.getElementById('announcements-empty');

    try {
        const response = await fetch(`${API_URL}/announcements`);
        if (response.ok) {
            const list = await response.json();
            if (list.length > 0) {
                emptyEl.style.display = 'none';
                listEl.innerHTML = list.map(item => `
                    <div class="card" style="padding: 1.5rem; border-left: 4px solid var(--primary-light);">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; flex-wrap: wrap;">
                            <h4 style="color: white; font-size: 1.1rem; margin: 0;">${item.title}</h4>
                            <span class="score-desc" style="font-size: 0.8rem; margin: 0;">${new Date(item.createdAt).toLocaleString('tr-TR')}</span>
                        </div>
                        <p class="score-desc" style="color: var(--text-secondary); line-height: 1.5; margin: 0;">${item.content}</p>
                    </div>
                `).join('');
            } else {
                emptyEl.style.display = 'block';
                listEl.innerHTML = '';
            }
        }
    } catch (err) {
        console.error('Duyurular yüklenemedi', err);
    }
}

// --- NEW COMPONENT: Admin Panel ---

async function loadAdminPanel() {
    // 1. Fetch Stats
    try {
        const statsRes = await fetch(`${API_URL}/admin/stats`, { headers: getAuthHeaders() });
        if (statsRes.ok) {
            const stats = await statsRes.json();
            document.getElementById('admin-stat-users').innerText = stats.totalUsers;
            document.getElementById('admin-stat-analyses').innerText = stats.totalAnalyses;
            document.getElementById('admin-stat-premium').innerText = stats.premiumUsers;
        }
    } catch (err) {
        console.error('Admin istatistikleri alınamadı', err);
    }

    // 2. Fetch Users list
    try {
        const usersRes = await fetch(`${API_URL}/admin/users`, { headers: getAuthHeaders() });
        if (usersRes.ok) {
            const users = await usersRes.json();
            const rowsEl = document.getElementById('admin-user-rows');
            rowsEl.innerHTML = users.map(user => `
                <tr>
                    <td>${user.id}</td>
                    <td><strong>${user.fullName}</strong></td>
                    <td>${user.email}</td>
                    <td>${user.isPremium ? t('admin-unlimited-text', 'Sınırsız') : user.credits}</td>
                    <td>
                        <span class="badge" style="margin-bottom: 0; background: ${user.isPremium ? 'rgba(124, 58, 237, 0.15); color: var(--primary-light)' : 'rgba(255,255,255,0.05); color: var(--text-secondary)'}; border: none;">
                            ${user.isPremium ? t('admin-premium-text', 'Premium') : t('admin-standard-text', 'Standart')}
                        </span>
                    </td>
                    <td>
                        <div style="display: flex; gap: 0.5rem;">
                            <button class="admin-btn primary" onclick="changeCreditsPrompt(${user.id}, ${user.credits})">${t('admin-edit-credits-btn', 'Kredi Düzenle')}</button>
                            <button class="admin-btn secondary" onclick="toggleUserPremium(${user.id}, ${!user.isPremium})">
                                ${user.isPremium ? t('admin-cancel-premium-btn', 'Premium İptal') : t('admin-make-premium-btn', 'Premium Yap')}
                            </button>
                        </div>
                    </td>
                </tr>
            `).join('');
        }
    } catch (err) {
        console.error('Kullanıcı listesi alınamadı', err);
    }
}

async function changeCreditsPrompt(userId, currentCredits) {
    const newCredits = prompt(t('admin-prompt-new-credits', 'Lütfen yeni kredi değerini girin:'), currentCredits);
    if (newCredits === null) return;
    
    const parsed = parseInt(newCredits);
    if (isNaN(parsed)) {
        alert(t('admin-alert-invalid-number', 'Lütfen geçerli bir sayı girin.'));
        return;
    }

    try {
        const res = await fetch(`${API_URL}/admin/users/${userId}/credits?credits=${parsed}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (res.ok) {
            showToast(t('toast-admin-credits-updated', 'Kullanıcı kredisi başarıyla güncellendi'), 'success');
            await loadAdminPanel();
            await loadUserInfo(); // Update headers credit display if active
        } else {
            showToast(t('toast-admin-credits-fail', 'Kredi düzenleme başarısız'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
    }
}

async function toggleUserPremium(userId, isPremium) {
    try {
        const res = await fetch(`${API_URL}/admin/users/${userId}/premium?isPremium=${isPremium}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (res.ok) {
            showToast(isPremium ? t('toast-admin-premium-success', 'Kullanıcı premium yapıldı!') : t('toast-admin-premium-cancelled', 'Kullanıcı premium üyeliği iptal edildi.'), 'success');
            await loadAdminPanel();
            await loadUserInfo();
        } else {
            showToast(t('toast-admin-action-fail', 'Aksiyon başarısız oldu'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
    }
}

async function handlePostAnnouncement(event) {
    event.preventDefault();
    const title = document.getElementById('announce-title').value;
    const content = document.getElementById('announce-content').value;

    try {
        const res = await fetch(`${API_URL}/admin/announcements`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ title, content })
        });

        if (res.ok) {
            showToast(t('toast-announce-success', 'Duyuru başarıyla yayınlandı!'), 'success');
            document.getElementById('announce-title').value = '';
            document.getElementById('announce-content').value = '';
            await loadAdminPanel();
        } else {
            const data = await res.json().catch(() => ({}));
            showToast(data.message || t('toast-announce-fail', 'Duyuru yayınlanamadı'), 'error');
        }
    } catch (err) {
        showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
    }
}

// --- NEW COMPONENT: Settings ---

async function loadSettingsForm() {
    try {
        const response = await fetch(`${API_URL}/auth/me`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const user = await response.json();
            document.getElementById('settings-name').value = user.fullName || '';
            const newEmailEl = document.getElementById('settings-new-email');
            if (newEmailEl) newEmailEl.value = user.email || '';

            // If the user's email is a placeholder from GitHub OAuth
            if (user.email && user.email.startsWith('github-needs-email-')) {
                const group = document.getElementById('current-password-group');
                if (group) group.style.display = 'none';
            } else {
                const group = document.getElementById('current-password-group');
                if (group) group.style.display = 'block';
            }
        }
    } catch (err) {
        showToast(t('toast-profile-load-fail', 'Kullanıcı bilgileri yüklenemedi'), 'error');
    }
}

let emailChangeStep = 1; // 1: request, 2: confirm
async function handleRequestEmailChange(event) {
    event.preventDefault();
    const newEmail = document.getElementById('settings-new-email').value;
    const currentPassword = document.getElementById('settings-email-current-password').value;
    const code = document.getElementById('settings-email-code').value;

    const submitBtn = document.getElementById('email-change-submit-btn');

    if (emailChangeStep === 1) {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = t('btn-email-requesting', 'Kod Gönderiliyor...');
        }
        try {
            const response = await fetch(`${API_URL}/auth/profile/request-email-change`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ newEmail, currentPassword })
            });
            if (response.ok) {
                showToast(t('toast-email-code-sent', 'Doğrulama kodu e-posta adresinize gönderildi.'), 'success');
                document.getElementById('email-change-code-group').style.display = 'block';
                emailChangeStep = 2;
                if (submitBtn) submitBtn.innerText = t('btn-email-confirm', 'E-postayı Güncelle');
            } else {
                const data = await response.json();
                showToast(data.message || t('toast-code-send-fail', 'Kod gönderilemedi'), 'error');
            }
        } catch (err) {
            showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        } finally {
            if (submitBtn && emailChangeStep === 1) {
                submitBtn.disabled = false;
                submitBtn.innerText = t('btn-email-send-code', 'Doğrulama Kodu Gönder');
            }
        }
    } else if (emailChangeStep === 2) {
        if (!code || code.length !== 6) {
            showToast(t('toast-code-length-error', 'Lütfen 6 haneli doğrulama kodunu girin.'), 'error');
            return;
        }
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = t('btn-email-confirming', 'E-posta Güncelleniyor...');
        }
        try {
            const response = await fetch(`${API_URL}/auth/profile/confirm-email-change`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ newEmail, code })
            });
            const data = await response.json();
            if (response.ok) {
                localStorage.setItem('devradar_token', data.token);
                localStorage.setItem('devradar_email', data.email);
                showToast(t('toast-email-updated', 'E-posta adresiniz başarıyla güncellendi!'), 'success');
                emailChangeStep = 1;
                document.getElementById('email-change-code-group').style.display = 'none';
                document.getElementById('settings-email-code').value = '';
                document.getElementById('settings-email-current-password').value = '';
                if (submitBtn) submitBtn.innerText = t('btn-email-send-code', 'Doğrulama Kodu Gönder');
                await loadUserInfo();
            } else {
                showToast(data.message || t('toast-verify-fail', 'Doğrulama başarısız'), 'error');
            }
        } catch (err) {
            showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    }
}

let passwordChangeStep = 1; // 1: request, 2: confirm
async function handleRequestPasswordChange(event) {
    event.preventDefault();
    const newPassword = document.getElementById('settings-new-password').value;
    const code = document.getElementById('settings-password-code').value;

    const submitBtn = document.getElementById('password-change-submit-btn');

    if (passwordChangeStep === 1) {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = t('btn-email-requesting', 'Kod Gönderiliyor...');
        }
        try {
            const response = await fetch(`${API_URL}/auth/profile/request-password-change`, {
                method: 'POST',
                headers: getAuthHeaders()
            });
            if (response.ok) {
                showToast(t('toast-pwd-code-sent', 'Doğrulama kodu e-posta adresinize gönderildi.'), 'success');
                document.getElementById('password-change-code-group').style.display = 'block';
                passwordChangeStep = 2;
                if (submitBtn) submitBtn.innerText = t('btn-password-confirm', 'Şifreyi Güncelle');
            } else {
                const data = await response.json();
                showToast(data.message || t('toast-code-send-fail', 'Kod gönderilemedi'), 'error');
            }
        } catch (err) {
            showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        } finally {
            if (submitBtn && passwordChangeStep === 1) {
                submitBtn.disabled = false;
                submitBtn.innerText = t('btn-email-send-code', 'Doğrulama Kodu Gönder');
            }
        }
    } else if (passwordChangeStep === 2) {
        if (!code || code.length !== 6) {
            showToast(t('toast-code-length-error', 'Lütfen 6 haneli doğrulama kodunu girin.'), 'error');
            return;
        }
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = t('btn-password-confirming', 'Şifre Güncelleniyor...');
        }
        try {
            const response = await fetch(`${API_URL}/auth/profile/confirm-password-change`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ newPassword, code })
            });
            if (response.ok) {
                showToast(t('toast-pwd-updated', 'Şifreniz başarıyla güncellendi!'), 'success');
                passwordChangeStep = 1;
                document.getElementById('password-change-code-group').style.display = 'none';
                document.getElementById('settings-password-code').value = '';
                document.getElementById('settings-new-password').value = '';
                if (submitBtn) submitBtn.innerText = t('btn-email-send-code', 'Doğrulama Kodu Gönder');
                await loadUserInfo();
            } else {
                const data = await response.json();
                showToast(data.message || t('toast-verify-fail', 'Doğrulama başarısız'), 'error');
            }
        } catch (err) {
            showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    }
}

let modalEmailStep = 1;
async function handleModalEmailChange(event) {
    event.preventDefault();
    const newEmail = document.getElementById('modal-new-email').value;
    const code = document.getElementById('modal-email-code').value;
    const submitBtn = document.getElementById('modal-email-submit-btn');

    if (modalEmailStep === 1) {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = t('btn-email-requesting', 'Kod Gönderiliyor...');
        }
        try {
            const response = await fetch(`${API_URL}/auth/profile/request-email-change`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ newEmail })
            });
            if (response.ok) {
                showToast(t('toast-email-code-sent', 'Doğrulama kodu e-posta adresinize gönderildi.'), 'success');
                document.getElementById('modal-email-code-group').style.display = 'block';
                modalEmailStep = 2;
                if (submitBtn) submitBtn.innerText = t('btn-email-modal-confirm', 'E-postayı Doğrula ve Kaydet');
            } else {
                const data = await response.json();
                showToast(data.message || t('toast-conn-error', 'Hata oluştu'), 'error');
            }
        } catch (err) {
            showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        } finally {
            if (submitBtn && modalEmailStep === 1) {
                submitBtn.disabled = false;
                submitBtn.innerText = t('btn-email-send-code', 'Doğrulama Kodu Gönder');
            }
        }
    } else if (modalEmailStep === 2) {
        if (!code || code.length !== 6) {
            showToast(t('toast-code-length-error', 'Lütfen 6 haneli doğrulama kodunu girin.'), 'error');
            return;
        }
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = t('btn-email-modal-confirming', 'Doğrulanıyor...');
        }
        try {
            const response = await fetch(`${API_URL}/auth/profile/confirm-email-change`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ newEmail, code })
            });
            const data = await response.json();
            if (response.ok) {
                localStorage.setItem('devradar_token', data.token);
                localStorage.setItem('devradar_email', data.email);
                showToast(t('toast-email-updated', 'E-posta adresiniz başarıyla tanımlandı!'), 'success');
                document.getElementById('github-email-modal').style.display = 'none';
                await loadUserInfo();
            } else {
                showToast(data.message || t('toast-verify-fail', 'Doğrulama başarısız'), 'error');
            }
        } catch (err) {
            showToast(t('toast-conn-error', 'Sunucu ile bağlantı kurulamadı'), 'error');
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    }
}

function checkGithubEmailRequirement() {
    const userEmail = localStorage.getItem('devradar_email');
    if (userEmail && userEmail.startsWith('github-needs-email-')) {
        const modal = document.getElementById('github-email-modal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }
}

// Init Dashboard
document.addEventListener('DOMContentLoaded', () => {
    // Only run if we are on the dashboard
    if (document.getElementById('user-display')) {
        const token = localStorage.getItem('devradar_token');
        if (!token) {
            window.location.href = '/auth?mode=login';
            return;
        }

        const name = localStorage.getItem('devradar_name');
        document.getElementById('user-display').innerText = name || 'Geliştirici';

        loadProfile();
        loadUserInfo();
        checkGithubEmailRequirement();

        // Add Enter key listener for Agent Chat Input
        const chatInput = document.getElementById('agent-chat-input');
        if (chatInput) {
            chatInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleAgentChatSubmit();
                }
            });
        }
    }
});

// AI Agent Live Chat Functions
let agentChatHistory = [];

async function loadAgentProjectContexts() {
    const selectEl = document.getElementById('agent-project-context');
    if (!selectEl) return;
    
    try {
        const response = await fetch(`${API_URL}/analysis/history`, {
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            const list = await response.json();
            selectEl.innerHTML = `<option value="">-- Proje Seçilmedi --</option>`;
            
            list.forEach(project => {
                const opt = document.createElement('option');
                opt.value = project.id;
                opt.dataset.name = project.projectName;
                opt.dataset.desc = project.description || '';
                opt.dataset.lang = project.targetLanguage || '';
                opt.innerText = project.projectName;
                selectEl.appendChild(opt);
            });
        }
    } catch (err) {
        console.error('Proje bağlamları yüklenemedi:', err);
    }
}

function useSuggestion(text) {
    const input = document.getElementById('agent-chat-input');
    if (input) {
        input.value = text;
        input.focus();
    }
}

function clearAgentChat() {
    agentChatHistory = [];
    const container = document.getElementById('agent-chat-messages');
    if (container) {
        container.innerHTML = `
            <div id="agent-chat-empty" class="h-full flex flex-col items-center justify-center text-center py-12">
                <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-signal/20 to-ember/20 border border-white/10 flex items-center justify-center text-4xl mb-6 shadow-[0_0_40px_rgba(232,255,71,0.08)]">
                    🤖
                </div>
                <h3 class="font-display font-bold text-xl text-mist-100 mb-2">DevRadar AI ile Proje Geliştir</h3>
                <p class="text-sm text-mist-700 max-w-md mb-8 leading-relaxed">
                    Kod yaz, hata ayıkla, mimari tasarla — her konuda yardım almak için sadece mesaj gönder.
                    Üstten proje bağlamı seçersen AI projeyi tanıyarak cevap verir.
                </p>
                <div class="flex flex-wrap gap-2 justify-center max-w-2xl">
                    <button onclick="useSuggestion('Express.js ile JWT authentication nasıl yapılır? Middleware dahil tam örnek yazar mısın?')" class="px-4 py-2 bg-white/5 hover:bg-signal/10 border border-white/10 hover:border-signal/40 rounded-full text-xs text-mist-700 hover:text-mist-100 transition-all">🔑 JWT Auth Middleware</button>
                    <button onclick="useSuggestion('Spring Boot projesinde veritabanı şeması nasıl tasarlanır? JPA entity örnekleri yazar mısın?')" class="px-4 py-2 bg-white/5 hover:bg-signal/10 border border-white/10 hover:border-signal/40 rounded-full text-xs text-mist-700 hover:text-mist-100 transition-all">🗄️ Spring Boot DB Şeması</button>
                    <button onclick="useSuggestion('React app\'de lazy loading ve code splitting nasıl uygulanır?')" class="px-4 py-2 bg-white/5 hover:bg-signal/10 border border-white/10 hover:border-signal/40 rounded-full text-xs text-mist-700 hover:text-mist-100 transition-all">⚡ React Lazy Loading</button>
                    <button onclick="useSuggestion('Python FastAPI ile basit bir REST API nasıl yazılır? Örnek endpointler göster.')" class="px-4 py-2 bg-white/5 hover:bg-signal/10 border border-white/10 hover:border-signal/40 rounded-full text-xs text-mist-700 hover:text-mist-100 transition-all">🐍 FastAPI REST Örneği</button>
                    <button onclick="useSuggestion('Docker ile bir Node.js uygulamasını containerize etmek için Dockerfile yazar mısın?')" class="px-4 py-2 bg-white/5 hover:bg-signal/10 border border-white/10 hover:border-signal/40 rounded-full text-xs text-mist-700 hover:text-mist-100 transition-all">🐳 Docker & Node.js</button>
                    <button onclick="useSuggestion('SQL ve NoSQL veritabanları arasındaki farklar nelerdir? Hangi durumda hangisini kullanmalıyım?')" class="px-4 py-2 bg-white/5 hover:bg-signal/10 border border-white/10 hover:border-signal/40 rounded-full text-xs text-mist-700 hover:text-mist-100 transition-all">💾 SQL vs NoSQL</button>
                </div>
            </div>
        `;
    }
    showToast('Sohbet sıfırlandı.', 'info');
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

function formatChatMessage(text) {
    if (!text) return '';
    
    let escaped = escapeHtml(text);
    
    // Parse thinking blocks first
    const thinkingRegex = /&lt;thinking&gt;([\s\S]*?)&lt;\/thinking&gt;/g;
    escaped = escaped.replace(thinkingRegex, (match, thought) => {
        const cleanThought = thought.trim().replace(/\n/g, '<br>');
        return `
            <details class="group mb-4 border border-emerald-500/20 bg-emerald-500/5 rounded-xl overflow-hidden text-left">
                <summary class="flex items-center gap-2 px-4 py-2 cursor-pointer bg-emerald-500/10 hover:bg-emerald-500/20 transition-colors">
                    <span class="text-sm">🤔</span>
                    <span class="text-xs font-bold text-emerald-400 tracking-wider">Ajanın Düşünce Süreci & Planı</span>
                    <span class="ml-auto text-emerald-400/50 group-open:rotate-180 transition-transform">▼</span>
                </summary>
                <div class="p-4 text-xs text-mist-700 leading-relaxed border-t border-emerald-500/10">
                    ${cleanThought}
                </div>
            </details>
        `;
    });
        
    // Regexp to match code blocks: ```lang ... ```
    // ?:(.*?)\n catches language or empty space until newline
    const codeBlockRegex = /```(.*?)?\n([\s\S]*?)```/g;
    
    let formatted = escaped.replace(codeBlockRegex, (match, lang, code) => {
        const uniqueId = 'code-' + Math.random().toString(36).substr(2, 9);
        const cleanCode = code ? code.trim() : '';
        const displayLang = lang && lang.trim() ? lang.trim().toUpperCase() : 'CODE';
        
        return `
            <div class="code-block-wrapper my-4 border border-white/10 rounded-xl overflow-hidden bg-ink-950/90 text-left">
                <div class="flex items-center justify-between px-4 py-2 bg-white/5 border-b border-white/10">
                    <span class="text-xs text-mist-700 font-mono font-bold tracking-wider">${displayLang}</span>
                    <button onclick="copyChatCode('${uniqueId}')" class="text-xs text-purple-400 hover:text-purple-300 font-semibold transition-colors flex items-center gap-1">
                        <span>📋</span> <span>Kopyala</span>
                    </button>
                </div>
                <pre id="${uniqueId}" class="p-4 overflow-x-auto text-xs font-mono text-mist-100 whitespace-pre leading-relaxed select-all">${cleanCode}</pre>
            </div>
        `;
    });
    
    // Parse basic markdown: bold and inline code
    // We only do this outside of code blocks and details blocks
    const parts = formatted.split(/(<div class="code-block-wrapper[\s\S]*?<\/div>|<details class="group[\s\S]*?<\/details>)/g);
    for (let i = 0; i < parts.length; i++) {
        if (!parts[i]) continue;
        if (!parts[i].startsWith('<div class="code-block-wrapper') && !parts[i].startsWith('<details class="group')) {
            // Convert inline code `code`
            let textPart = parts[i].replace(/`([^`]+)`/g, '<code class="px-1.5 py-0.5 rounded bg-white/10 text-emerald-300 font-mono text-[11px]">$1</code>');
            // Convert bold **text**
            textPart = textPart.replace(/\*\*([^*]+)\*\*/g, '<strong class="font-bold text-white">$1</strong>');
            // Convert basic lists (lines starting with - or * )
            textPart = textPart.replace(/^(?:-|\*)\s+(.+)$/gm, '<li class="ml-4 list-disc">$1</li>');
            // Convert newlines to <br>
            parts[i] = textPart.replace(/\n/g, '<br>');
        }
    }
    
    return parts.join('');
}

function copyChatCode(elementId) {
    const el = document.getElementById(elementId);
    if (!el) return;
    
    navigator.clipboard.writeText(el.innerText).then(() => {
        showToast('Kod kopyalandı!', 'success');
    }).catch(() => {
        showToast('Kopyalama başarısız oldu.', 'error');
    });
}

async function handleAgentChatSubmit(event) {
    if (event) event.preventDefault();
    
    const inputEl = document.getElementById('agent-chat-input');
    if (!inputEl) return;
    
    const promptText = inputEl.value.trim();
    if (!promptText) return;
    
    // Clear input
    inputEl.value = '';
    
    const container = document.getElementById('agent-chat-messages');
    const emptyState = document.getElementById('agent-chat-empty');
    const typingIndicator = document.getElementById('agent-chat-typing');
    const submitBtn = document.getElementById('agent-chat-submit-btn');
    const aiProvider = document.getElementById('agent-provider').value;
    
    // Hide empty state if visible
    if (emptyState) emptyState.style.display = 'none';
    
    // Append User Message to history and DOM
    agentChatHistory.push({ role: 'user', content: promptText });
    
    const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    const userMsgHtml = `
        <div class="flex gap-3 justify-end items-start">
            <div class="max-w-[85%] bg-violet-600/25 border border-violet-500/30 rounded-2xl rounded-tr-sm px-5 py-3.5 shadow-[0_2px_20px_rgba(124,58,237,0.15)]">
                <p class="text-sm text-mist-100 whitespace-pre-wrap leading-relaxed">${escapeHtml(promptText)}</p>
                <span class="text-[9px] text-violet-400/60 block mt-2 text-right font-mono">${timeStr}</span>
            </div>
            <div class="w-9 h-9 rounded-full bg-gradient-to-br from-violet-600 to-violet-800 flex items-center justify-center text-xs font-bold text-white shrink-0 border border-violet-500/30 shadow-lg">
                Sen
            </div>
        </div>
    `;
    
    if (container) {
        container.insertAdjacentHTML('beforeend', userMsgHtml);
        container.scrollTop = container.scrollHeight;
    }
    
    // Show typing indicator
    if (typingIndicator) {
        typingIndicator.classList.remove('hidden');
        if (container) container.scrollTop = container.scrollHeight;
    }
    
    if (submitBtn) {
        submitBtn.disabled = true;
    }
    
    try {
        // Build natural conversational context
        let fullPromptContext = "";
        
        const selectedProj = document.getElementById('agent-project-context');
        if (selectedProj && selectedProj.value) {
            const opt = selectedProj.options[selectedProj.selectedIndex];
            fullPromptContext += `[Active project: ${opt.dataset.name} | Language: ${opt.dataset.lang}${opt.dataset.desc ? ' | ' + opt.dataset.desc.substring(0, 120) : ''}]\n\n`;
        }
        
        // Add conversation history in natural chat format
        const historyForContext = agentChatHistory.slice(0, -1); // exclude the current message we just added
        if (historyForContext.length > 0) {
            historyForContext.forEach(msg => {
                fullPromptContext += msg.role === 'user'
                    ? `Human: ${msg.content}\n\n`
                    : `Assistant: ${msg.content}\n\n`;
            });
        }
        
        // Append the current user message and prompt the assistant to reply
        fullPromptContext += `Human: ${promptText}\n\nAssistant:`;
        
        const lang = getLanguage();
        const response = await fetch(`${API_URL}/code-assistant`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ 
                prompt: fullPromptContext, 
                aiProvider, 
                language: lang 
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            
            // Add to history
            agentChatHistory.push({ role: 'assistant', content: data.response });
            
            const providerLabel = data.aiProviderUsed ? data.aiProviderUsed.toUpperCase() : 'AI';
            const modelLabel = data.modelUsed && data.modelUsed !== 'unknown' ? data.modelUsed : '';
            
            const badgeContent = modelLabel ? `${providerLabel} (${modelLabel})` : providerLabel;

            const assistantMsgHtml = `
                <div class="flex gap-3 justify-start items-start">
                    <div class="w-9 h-9 rounded-full bg-gradient-to-br from-signal/30 to-emerald-500/20 border border-signal/30 flex items-center justify-center text-[10px] font-bold text-signal shrink-0 shadow-[0_0_12px_rgba(232,255,71,0.15)]">
                        AI
                    </div>
                    <div class="max-w-[95%] w-full bg-ink-800/60 border border-white/8 rounded-2xl rounded-tl-sm px-5 py-4 shadow-[0_2px_20px_rgba(0,0,0,0.3)]">
                        <div class="flex items-center gap-2 mb-3">
                            <span class="text-[9px] px-2 py-0.5 rounded-full font-bold tracking-wider border" style="background:rgba(232,255,71,0.08); color:#e8ff47; border-color:rgba(232,255,71,0.2);">${badgeContent}</span>
                            <span class="text-[9px] text-mist-900 font-mono">${timeStr}</span>
                        </div>
                        <div class="text-sm text-mist-100 leading-relaxed space-y-2">
                            ${formatChatMessage(data.response)}
                        </div>
                    </div>
                </div>
            `;
            
            if (container) {
                container.insertAdjacentHTML('beforeend', assistantMsgHtml);
            }
            
            await loadUserInfo();
        } else {
            const errData = await response.json();
            showToast(errData.message || 'Bir hata oluştu.', 'error');
        }
    } catch (err) {
        showToast('Bağlantı hatası oluştu.', 'error');
    } finally {
        // Hide typing indicator
        if (typingIndicator) {
            typingIndicator.classList.add('hidden');
        }
        
        if (submitBtn) {
            submitBtn.disabled = false;
        }
        
        if (container) {
            container.scrollTop = container.scrollHeight;
        }
    }
}
