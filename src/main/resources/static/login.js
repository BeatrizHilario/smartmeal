document.addEventListener("DOMContentLoaded", () => {
    // Seletores dos Botões de Gatilho
    const btnRegistrar = document.getElementById('open-register');
    const btnLogin = document.getElementById('open-login');
    const linkSaberMais = document.getElementById('open-about');

    // Seletores dos Modais Overlays
    const modalRegister = document.getElementById('modal-register');
    const modalLogin = document.getElementById('modal-login');
    const modalAbout = document.getElementById('modal-about');
    const mainContent = document.getElementById('main-content');

    // Função genérica para abrir um modal aplicando blur na tela de fundo
    function abrirModal(modal) {
        modal.classList.add('ativo');
        mainContent.style.filter = "blur(5px)"; // Desfoca suavemente o fundo
    }

    // Função genérica para fechar todos os modais e limpar o blur
    function fecharModais() {
        document.querySelectorAll('.modal-overlay').forEach(modal => {
            modal.classList.remove('ativo');
        });
        mainContent.style.filter = "none"; // Remove o desfoque
    }

    // Ouvintes de Clique para Abrir
    if(btnRegistrar) btnRegistrar.addEventListener('click', () => abrirModal(modalRegister));
    if(btnLogin) btnLogin.addEventListener('click', () => abrirModal(modalLogin));
    if(linkSaberMais) linkSaberMais.addEventListener('click', (e) => { e.preventDefault(); abrirModal(modalAbout); });

    // Ouvintes de Clique para Fechar (Nos botões "X")
    document.querySelectorAll('.btn-fechar').forEach(botao => {
        botao.addEventListener('click', fecharModais);
    });

    // Fechar se o usuário clicar na área cinza fora do card do formulário
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if(e.target === overlay) fecharModais();
        });
    });
});

function mostrarToast(mensagem, tipo) {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');

        // Estilo base do toast (inicia fora da tela para fazer a animação de entrada)
        toast.className = `flex items-center gap-3 px-5 py-4 bg-white rounded-2xl shadow-xl transform transition-all duration-500 translate-x-full opacity-0 max-w-sm w-full border-l-4 pointer-events-auto`;

        let icone = '';
        if (tipo === 'erro') {
            toast.classList.add('border-red-500');
            icone = `<div class="w-8 h-8 rounded-full bg-red-50 flex items-center justify-center text-red-500 shrink-0"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></div>`;
        } else {
            toast.classList.add('border-[#84A98C]'); // Verde Salvia
            icone = `<div class="w-8 h-8 rounded-full bg-[#84A98C]/10 flex items-center justify-center text-[#84A98C] shrink-0"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg></div>`;
        }

        // Monta o visual interno do toast
        toast.innerHTML = `${icone} <p class="text-sm font-medium text-gray-700 font-sans">${mensagem}</p>`;

        container.appendChild(toast);

        // Dispara a animação de entrada (deslizando da direita)
        setTimeout(() => {
            toast.classList.remove('translate-x-full', 'opacity-0');
        }, 100);

        // Remove automaticamente após 4 segundos
        setTimeout(() => {
            toast.classList.add('translate-x-full', 'opacity-0');
            setTimeout(() => toast.remove(), 500); // Espera a animação de saída terminar
        }, 4000);
    }

    /* O Thymeleaf injeta o valor do Java aqui. */
    var mensagemErro = /*[[${erro}]]*/ null;
    if (mensagemErro) mostrarToast(mensagemErro, 'erro');

    var mensagemSucesso = /*[[${sucesso}]]*/ null;
    if (mensagemSucesso) mostrarToast(mensagemSucesso, 'sucesso');