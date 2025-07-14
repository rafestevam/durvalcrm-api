// Funcionalidades JavaScript para o tema DurvalCRM
document.addEventListener('DOMContentLoaded', function() {
    
    // Adicionar classes Tailwind dinamicamente se necessário
    function initializeTailwindClasses() {
        const forms = document.querySelectorAll('form');
        forms.forEach(form => {
            form.classList.add('space-y-6');
        });
        
        const inputs = document.querySelectorAll('input:not([type="checkbox"]):not([type="submit"])');
        inputs.forEach(input => {
            input.classList.add('block', 'w-full', 'rounded-md', 'border-gray-300', 'shadow-sm', 'focus:border-blue-500', 'focus:ring-blue-500', 'sm:text-sm');
        });
        
        const labels = document.querySelectorAll('label');
        labels.forEach(label => {
            if (!label.querySelector('input[type="checkbox"]')) {
                label.classList.add('block', 'text-sm', 'font-medium', 'text-gray-700');
            }
        });
    }
    
    // Adicionar animações de entrada
    function addEntranceAnimations() {
        const loginContainer = document.querySelector('.bg-white');
        if (loginContainer) {
            loginContainer.style.opacity = '0';
            loginContainer.style.transform = 'translateY(10px)';
            loginContainer.style.transition = 'all 0.3s ease-out';
            
            setTimeout(() => {
                loginContainer.style.opacity = '1';
                loginContainer.style.transform = 'translateY(0)';
            }, 100);
        }
    }
    
    // Melhorar acessibilidade dos formulários
    function enhanceAccessibility() {
        const inputs = document.querySelectorAll('input');
        inputs.forEach(input => {
            // Adicionar label association se não existir
            if (input.id && !input.getAttribute('aria-labelledby')) {
                const label = document.querySelector(`label[for="${input.id}"]`);
                if (label && !label.id) {
                    label.id = `${input.id}-label`;
                    input.setAttribute('aria-labelledby', label.id);
                }
            }
            
            // Adicionar descrição de erro
            const errorSpan = input.parentNode.querySelector('.kc-input-error-message');
            if (errorSpan && !errorSpan.id) {
                errorSpan.id = `${input.id}-error`;
                input.setAttribute('aria-describedby', errorSpan.id);
            }
        });
    }
    
    // Adicionar loading state aos botões de submit
    function enhanceSubmitButtons() {
        const submitButtons = document.querySelectorAll('input[type="submit"], button[type="submit"]');
        submitButtons.forEach(button => {
            button.addEventListener('click', function() {
                if (this.form && this.form.checkValidity()) {
                    this.disabled = true;
                    this.value = this.dataset.loadingText || 'Processando...';
                    
                    // Adicionar spinner
                    const spinner = document.createElement('span');
                    spinner.innerHTML = `
                        <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white inline" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                    `;
                    this.parentNode.insertBefore(spinner, this);
                }
            });
        });
    }
    
    // Adicionar validação em tempo real
    function addRealTimeValidation() {
        const inputs = document.querySelectorAll('input[required]');
        inputs.forEach(input => {
            input.addEventListener('blur', function() {
                validateField(this);
            });
            
            input.addEventListener('input', function() {
                if (this.classList.contains('invalid')) {
                    validateField(this);
                }
            });
        });
    }
    
    function validateField(field) {
        const isValid = field.checkValidity();
        const errorSpan = field.parentNode.querySelector('.kc-input-error-message');
        
        if (!isValid) {
            field.classList.add('invalid');
            field.setAttribute('aria-invalid', 'true');
            
            if (!errorSpan) {
                const newError = document.createElement('span');
                newError.className = 'kc-input-error-message text-red-600 text-xs mt-1 block';
                newError.textContent = field.validationMessage;
                field.parentNode.appendChild(newError);
            }
        } else {
            field.classList.remove('invalid');
            field.setAttribute('aria-invalid', 'false');
            
            if (errorSpan && !field.parentNode.querySelector('.server-error')) {
                errorSpan.remove();
            }
        }
    }
    
    // Detectar tema escuro do sistema
    function detectDarkMode() {
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            document.body.classList.add('dark-mode');
        }
        
        // Listener para mudanças
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
            if (e.matches) {
                document.body.classList.add('dark-mode');
            } else {
                document.body.classList.remove('dark-mode');
            }
        });
    }
    
    // Inicializar todas as funcionalidades
    initializeTailwindClasses();
    addEntranceAnimations();
    enhanceAccessibility();
    enhanceSubmitButtons();
    addRealTimeValidation();
    detectDarkMode();
    
    // Adicionar log para debugging
    console.log('DurvalCRM Keycloak theme initialized');
});

// Utilitários
window.DurvalCRMTheme = {
    showLoading: function(message = 'Carregando...') {
        const overlay = document.createElement('div');
        overlay.className = 'fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50';
        overlay.innerHTML = `
            <div class="bg-white p-6 rounded-lg shadow-lg flex items-center space-x-3">
                <svg class="animate-spin h-5 w-5 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span class="text-gray-700">${message}</span>
            </div>
        `;
        document.body.appendChild(overlay);
        return overlay;
    },
    
    hideLoading: function(overlay) {
        if (overlay && overlay.parentNode) {
            overlay.parentNode.removeChild(overlay);
        }
    }
};