/**
 * Excuse Generator Frontend Client
 */

(function () {
    'use strict';

    // Application state
    const state = {
        currentLang: 'pl', // Default to Polish as requested in previous project context, or English
        currentCategory: 'all',
        languages: [],
        categories: [],
        translations: {},
        isGenerating: false,
        lastExcuse: null
    };

    // DOM Elements
    const elements = {
        html: document.documentElement,
        languageSelect: document.getElementById('languageSelect'),
        currentFlag: document.getElementById('currentFlag'),
        categoryPills: document.getElementById('categoryPills'),
        categoryBadge: document.getElementById('categoryBadge'),
        badgeIcon: document.getElementById('badgeIcon'),
        badgeText: document.getElementById('badgeText'),
        excuseCard: document.getElementById('excuseCard'),
        excuseText: document.getElementById('excuseText'),
        poolCountNumber: document.getElementById('poolCountNumber'),
        generateBtn: document.getElementById('generateBtn'),
        generateBtnText: document.getElementById('generateBtnText'),
        copyBtn: document.getElementById('copyBtn'),
        copyBtnText: document.getElementById('copyBtnText'),
        pageTitle: document.getElementById('pageTitle'),
        pageSubtitle: document.getElementById('pageSubtitle'),
        toast: document.getElementById('toast')
    };

    /**
     * Initialize Application
     */
    async function init() {
        try {
            await Promise.all([
                loadLanguages(),
                loadTranslations()
            ]);

            // Set initial language from browser if supported, else default to 'pl'
            const browserLang = (navigator.language || 'pl').split('-')[0].toLowerCase();
            if (state.languages.some(l => l.code === browserLang)) {
                state.currentLang = browserLang;
            } else {
                state.currentLang = 'pl';
            }

            elements.languageSelect.value = state.currentLang;
            updateFlagIcon();
            updateTextDirection();
            applyTranslations();

            await loadCategories();
            bindEvents();
        } catch (error) {
            console.error('Initialization error:', error);
            showToast('Failed to initialize application: ' + error.message, true);
        }
    }

    /**
     * API Fetch helpers
     */
    async function loadLanguages() {
        const res = await fetch('/api/languages');
        if (!res.ok) throw new Error('Failed to fetch languages');
        state.languages = await res.json();

        elements.languageSelect.innerHTML = '';
        state.languages.forEach(lang => {
            const opt = document.createElement('option');
            opt.value = lang.code;
            opt.textContent = `${lang.flag} ${lang.nativeName} (${lang.name})`;
            elements.languageSelect.appendChild(opt);
        });
    }

    async function loadTranslations() {
        const res = await fetch('/api/ui-translations');
        if (!res.ok) throw new Error('Failed to fetch translations');
        state.translations = await res.json();
    }

    async function loadCategories() {
        const res = await fetch(`/api/categories?lang=${encodeURIComponent(state.currentLang)}`);
        if (!res.ok) throw new Error('Failed to fetch categories');
        state.categories = await res.json();
        renderCategoryPills();
        updateActiveBadge();
    }

    /**
     * Render category buttons
     */
    function renderCategoryPills() {
        elements.categoryPills.innerHTML = '';

        state.categories.forEach(cat => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = `cat-pill ${cat.id === state.currentCategory ? 'active' : ''}`;
            btn.dataset.id = cat.id;

            const iconSpan = document.createElement('span');
            iconSpan.className = 'cat-pill-icon';
            iconSpan.textContent = cat.icon || '📌';

            const nameSpan = document.createElement('span');
            nameSpan.className = 'cat-pill-name';
            nameSpan.textContent = cat.name;

            const countSpan = document.createElement('span');
            countSpan.className = 'cat-pill-count';
            countSpan.textContent = cat.count;

            btn.appendChild(iconSpan);
            btn.appendChild(nameSpan);
            btn.appendChild(countSpan);

            btn.addEventListener('click', () => {
                state.currentCategory = cat.id;
                document.querySelectorAll('.cat-pill').forEach(p => p.classList.remove('active'));
                btn.classList.add('active');
                updateActiveBadge();
            });

            elements.categoryPills.appendChild(btn);
        });
    }

    /**
     * Update Category Badge inside card
     */
    function updateActiveBadge(overrideCatId, overrideCatName, overrideIcon) {
        const catId = overrideCatId || state.currentCategory;
        const currentCatObj = state.categories.find(c => c.id === catId);

        const icon = overrideIcon || (currentCatObj ? currentCatObj.icon : '✨');
        const name = overrideCatName || (currentCatObj ? currentCatObj.name : 'All Categories');

        elements.badgeIcon.textContent = icon;
        elements.badgeText.textContent = name;

        // Update count
        const total = currentCatObj ? currentCatObj.count : 80;
        elements.poolCountNumber.textContent = total;
    }

    /**
     * Switch Language
     */
    async function changeLanguage(newLang) {
        if (state.currentLang === newLang) return;
        state.currentLang = newLang;

        updateFlagIcon();
        updateTextDirection();
        applyTranslations();

        // Refresh category names in selected language
        await loadCategories();

        // If an excuse is already displayed and belongs to a specific category, re-request in new language
        if (state.lastExcuse && state.lastExcuse.text) {
            await generateExcuse(false);
        }
    }

    function updateFlagIcon() {
        const langObj = state.languages.find(l => l.code === state.currentLang);
        if (langObj) {
            elements.currentFlag.textContent = langObj.flag;
        }
    }

    function updateTextDirection() {
        const langObj = state.languages.find(l => l.code === state.currentLang);
        const dir = (langObj && langObj.dir) || 'ltr';
        elements.html.setAttribute('dir', dir);
        elements.html.setAttribute('lang', state.currentLang);
    }

    /**
     * Apply localized texts to all UI components
     */
    function applyTranslations() {
        const t = state.translations;
        const lang = state.currentLang;

        const getTrans = (key, fallback) => {
            if (t && t[key] && t[key][lang]) return t[key][lang];
            if (t && t[key] && t[key]['en']) return t[key]['en'];
            return fallback;
        };

        // Update data-i18n elements
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            const translation = getTrans(key, el.textContent);
            el.textContent = translation;
        });

        // Update Generate button
        elements.generateBtnText.textContent = getTrans('generateBtn', 'Generate Excuse');

        // Update Copy button if not currently in "copied" state
        if (!elements.copyBtn.classList.contains('copied')) {
            elements.copyBtnText.textContent = getTrans('copyBtn', 'Copy');
        }

        // If card is still in initial placeholder state, update placeholder text
        if (elements.excuseText.classList.contains('placeholder-state')) {
            elements.excuseText.textContent = getTrans('initialPrompt', 'Click the button below to roll a perfect excuse.');
        }
    }

    /**
     * Generate Random Excuse from API
     */
    async function generateExcuse(animate = true) {
        if (state.isGenerating) return;
        state.isGenerating = true;

        elements.generateBtn.disabled = true;
        elements.generateBtn.style.opacity = '0.75';

        try {
            const url = `/api/excuse/random?category=${encodeURIComponent(state.currentCategory)}&lang=${encodeURIComponent(state.currentLang)}`;
            const res = await fetch(url);
            if (!res.ok) throw new Error('Failed to generate excuse');
            const data = await res.json();

            state.lastExcuse = data;

            // Update text with animation
            elements.excuseText.classList.remove('placeholder-state');
            if (animate) {
                elements.excuseText.classList.remove('anim-in');
                // Force reflow
                void elements.excuseText.offsetWidth;
                elements.excuseText.classList.add('anim-in');
            }
            elements.excuseText.textContent = data.text;

            // Update category badge to reflect the specific category of the generated excuse
            updateActiveBadge(data.categoryId, data.categoryName, data.categoryIcon);

            if (data.totalAvailable) {
                elements.poolCountNumber.textContent = data.totalAvailable;
            }
        } catch (error) {
            console.error('Error fetching excuse:', error);
            showToast('Error: ' + error.message, true);
        } finally {
            state.isGenerating = false;
            elements.generateBtn.disabled = false;
            elements.generateBtn.style.opacity = '1';
        }
    }

    /**
     * Copy Excuse to Clipboard
     */
    async function copyToClipboard() {
        const textToCopy = elements.excuseText.textContent.trim();
        if (!textToCopy || elements.excuseText.classList.contains('placeholder-state')) {
            return;
        }

        try {
            await navigator.clipboard.writeText(textToCopy);

            const copyIcon = elements.copyBtn.querySelector('.copy-icon');
            const checkIcon = elements.copyBtn.querySelector('.check-icon');

            elements.copyBtn.classList.add('copied');
            copyIcon.classList.add('hidden');
            checkIcon.classList.remove('hidden');

            const t = state.translations;
            const lang = state.currentLang;
            const copiedText = (t && t['copiedBtn'] && t['copiedBtn'][lang]) || 'Copied!';
            elements.copyBtnText.textContent = copiedText;

            showToast(copiedText, false);

            setTimeout(() => {
                elements.copyBtn.classList.remove('copied');
                copyIcon.classList.remove('hidden');
                checkIcon.classList.add('hidden');
                const copyText = (t && t['copyBtn'] && t['copyBtn'][lang]) || 'Copy';
                elements.copyBtnText.textContent = copyText;
            }, 2000);
        } catch (err) {
            console.error('Failed to copy to clipboard', err);
            showToast('Could not copy to clipboard', true);
        }
    }

    /**
     * Toast notification
     */
    let toastTimeout = null;
    function showToast(message, isError = false) {
        clearTimeout(toastTimeout);
        elements.toast.textContent = message;
        elements.toast.style.background = isError ? '#ef4444' : '#10b981';
        elements.toast.classList.remove('hidden');

        toastTimeout = setTimeout(() => {
            elements.toast.classList.add('hidden');
        }, 2200);
    }

    /**
     * Bind Event Listeners
     */
    function bindEvents() {
        elements.languageSelect.addEventListener('change', (e) => {
            changeLanguage(e.target.value);
        });

        elements.generateBtn.addEventListener('click', () => {
            generateExcuse(true);
        });

        elements.copyBtn.addEventListener('click', () => {
            copyToClipboard();
        });

        // Spacebar shortcut
        window.addEventListener('keydown', (e) => {
            if (e.code === 'Space' && e.target === document.body) {
                e.preventDefault();
                generateExcuse(true);
            }
        });
    }

    // Start
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
