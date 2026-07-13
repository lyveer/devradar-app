// API Base URL
const API_URL = '/api';

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

// Switch between Login and Register on auth.html
function switchAuthMode(mode) {
    const title = document.getElementById('auth-title');
    const desc = document.getElementById('auth-desc');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');

    if (!title) return;

    if (mode === 'login') {
        title.innerText = 'Giriş Yap';
        desc.innerText = 'Lyver Software DevRadar paneline erişmek için bilgilerinizi girin.';
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
    } else {
        title.innerText = 'Hesap Oluştur';
        desc.innerText = 'Hemen ücretsiz kaydolun ve AI gücünü keşfedin.';
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        tabLogin.classList.remove('active');
        tabRegister.classList.add('active');
    }
}

// Authentication Logic
async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

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
            showToast('Giriş başarılı! Yönlendiriliyorsunuz...', 'success');
            setTimeout(() => window.location.href = '/dashboard', 1000);
        } else {
            showToast(data.message || 'Giriş başarısız', 'error');
        }
    } catch (err) {
        showToast('Sunucu ile bağlantı kurulamadı', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const fullName = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('devradar_token', data.token);
            localStorage.setItem('devradar_email', data.email);
            localStorage.setItem('devradar_name', data.fullName);
            showToast('Kayıt başarılı! Yönlendiriliyorsunuz...', 'success');
            setTimeout(() => window.location.href = '/dashboard', 1000);
        } else {
            showToast(data.message || 'Kayıt başarısız', 'error');
        }
    } catch (err) {
        showToast('Sunucu ile bağlantı kurulamadı', 'error');
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
                if (user.isPremium) {
                    creditDisplay.innerText = 'Kredi: Sınırsız';
                    creditDisplay.style.background = 'rgba(124, 58, 237, 0.15)';
                    creditDisplay.style.color = 'var(--primary-light)';
                    creditDisplay.style.borderColor = 'rgba(124, 58, 237, 0.25)';
                    if (subscribeBtn) subscribeBtn.style.display = 'none';
                } else {
                    creditDisplay.innerText = `Kredi: ${user.credits}`;
                    creditDisplay.style.background = 'rgba(16, 185, 129, 0.15)';
                    creditDisplay.style.color = '#10b981';
                    creditDisplay.style.borderColor = 'rgba(16, 185, 129, 0.25)';
                    if (subscribeBtn) subscribeBtn.style.display = 'inline-flex';
                }
            }
        }
    } catch (err) {
        console.error('Kullanıcı bilgileri yüklenemedi', err);
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
            showToast('Abonelik başarıyla tamamlandı! Artık sınırsız analiz yapabilirsiniz.', 'success');
            await loadUserInfo();
        } else {
            showToast('Abonelik işlemi gerçekleştirilemedi.', 'error');
        }
    } catch (err) {
        showToast('Bağlantı hatası', 'error');
    }
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
                document.getElementById('profile-specialization').value = profile.specialization || '';
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
                document.getElementById('profile-ai-empty').style.display = 'block';
            }
        }
    } catch (err) {
        showToast('Profil bilgileri yüklenemedi', 'error');
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
        showToast('Lütfen en az bir programlama dili seçin', 'error');
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
            showToast('Profil başarıyla güncellendi ve Yapay Zeka ile analiz edildi!', 'success');
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
            showToast('Profil kaydedilemedi', 'error');
        }
    } catch (err) {
        showToast('Bağlantı hatası', 'error');
    }
}

async function handleTriggerScoring() {
    const scoreBtn = document.getElementById('score-btn');
    scoreBtn.innerText = 'Analiz Ediliyor...';
    scoreBtn.disabled = true;

    try {
        const response = await fetch(`${API_URL}/profile/score`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            renderProfileScore(data.score, data.summary, data.strengths, data.weaknesses, data.recommendations);
            showToast('Profil AI Analizi tamamlandı!', 'success');
            await loadUserInfo(); // Update remaining credits display
        } else {
            const errData = await response.json().catch(() => ({}));
            showToast(errData.message || 'AI Puanlama başarısız oldu', 'error');
            scoreBtn.innerText = 'Profil Puanımı Analiz Et';
            scoreBtn.disabled = false;
        }
    } catch (err) {
        showToast('Bağlantı hatası', 'error');
        scoreBtn.innerText = 'Profil Puanımı Analiz Et';
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

    const submitBtn = document.getElementById('analyze-submit-btn');
    submitBtn.disabled = true;
    submitBtn.innerText = 'Yapay Zeka Analiz Ediyor...';

    document.getElementById('analysis-empty').style.display = 'none';
    document.getElementById('analysis-loading').style.display = 'block';
    document.getElementById('analysis-content').style.display = 'none';

    try {
        const response = await fetch(`${API_URL}/analysis`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ projectName, projectDescription, targetLanguage })
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
                    // Check completion checks from local storage
                    const storageKey = `progress_${localStorage.getItem('devradar_email')}_${item.id}`;
                    const savedChecks = JSON.parse(localStorage.getItem(storageKey) || '[]');
                    
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

function toggleChecklistItem(projectId, stepIdx, checkbox) {
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
                    <td>${user.isPremium ? 'Sınırsız' : user.credits}</td>
                    <td>
                        <span class="badge" style="margin-bottom: 0; background: ${user.isPremium ? 'rgba(124, 58, 237, 0.15); color: var(--primary-light)' : 'rgba(255,255,255,0.05); color: var(--text-secondary)'}; border: none;">
                            ${user.isPremium ? 'Premium' : 'Standart'}
                        </span>
                    </td>
                    <td>
                        <div style="display: flex; gap: 0.5rem;">
                            <button class="admin-btn primary" onclick="changeCreditsPrompt(${user.id}, ${user.credits})">Kredi Düzenle</button>
                            <button class="admin-btn secondary" onclick="toggleUserPremium(${user.id}, ${!user.isPremium})">
                                ${user.isPremium ? 'Premium İptal' : 'Premium Yap'}
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
    const newCredits = prompt("Lütfen yeni kredi değerini girin:", currentCredits);
    if (newCredits === null) return;
    
    const parsed = parseInt(newCredits);
    if (isNaN(parsed)) {
        alert("Lütfen geçerli bir sayı girin.");
        return;
    }

    try {
        const res = await fetch(`${API_URL}/admin/users/${userId}/credits?credits=${parsed}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (res.ok) {
            showToast('Kullanıcı kredisi başarıyla güncellendi', 'success');
            await loadAdminPanel();
            await loadUserInfo(); // Update headers credit display if active
        } else {
            showToast('Kredi düzenleme başarısız', 'error');
        }
    } catch (err) {
        showToast('Bağlantı hatası', 'error');
    }
}

async function toggleUserPremium(userId, isPremium) {
    try {
        const res = await fetch(`${API_URL}/admin/users/${userId}/premium?isPremium=${isPremium}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (res.ok) {
            showToast(isPremium ? 'Kullanıcı premium yapıldı!' : 'Kullanıcı premium üyeliği iptal edildi.', 'success');
            await loadAdminPanel();
            await loadUserInfo();
        } else {
            showToast('Aksiyon başarısız oldu', 'error');
        }
    } catch (err) {
        showToast('Bağlantı hatası', 'error');
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
            showToast('Duyuru başarıyla yayınlandı!', 'success');
            document.getElementById('announce-title').value = '';
            document.getElementById('announce-content').value = '';
            await loadAdminPanel();
        } else {
            const data = await res.json().catch(() => ({}));
            showToast(data.message || 'Duyuru yayınlanamadı', 'error');
        }
    } catch (err) {
        showToast('Bağlantı hatası', 'error');
    }
}

// Init Dashboard
document.addEventListener('DOMContentLoaded', () => {
    // Only run if we are on the dashboard
    if (window.location.pathname.endsWith('/dashboard') || window.location.pathname.endsWith('dashboard.html')) {
        const token = localStorage.getItem('devradar_token');
        if (!token) {
            window.location.href = '/auth?mode=login';
            return;
        }

        const name = localStorage.getItem('devradar_name');
        document.getElementById('user-display').innerText = name || 'Geliştirici';

        loadProfile();
        loadUserInfo();
    }
});
