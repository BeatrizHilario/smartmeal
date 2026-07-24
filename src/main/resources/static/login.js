document.addEventListener("DOMContentLoaded", () => {
    // Seletores dos Botões de Gatilho
    const btnRegistrar = document.getElementById('open-register');
    const btnLogin = document.getElementById('open-login');
    const linkSaberMais = document.getElementById('open-about');
    const linkRecuperar = document.getElementById('open-recuperar');
    const modalNovaSenha = document.getElementById('modal-nova-senha');
    const inputEmailLogin = document.querySelector('#modal-login input[name="email"]');
    const inputEmailEscondido = document.getElementById('email-escondido');

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
   if (linkRecuperar) {
       linkRecuperar.addEventListener('click', (e) => {
           e.preventDefault();

           // 1. Verifica se a pessoa digitou o email no login
           if (inputEmailLogin.value.trim() === '') {
               mostrarToast('Por favor, digite seu email no campo de login primeiro.', 'erro');
               return; // Para o código aqui e não abre a tela
           }

           // 2. Copia o email digitado para o campo invisível do nosso novo modal
           inputEmailEscondido.value = inputEmailLogin.value;

           // 3. Fecha o login e abre a tela de nova senha
           fecharModais();
           abrirModal(modalNovaSenha);
       });
   }
});

// Função para alternar a visibilidade da senha no Login
function toggleSenhaLogin() {
    const inputSenha = document.getElementById('senha-login');
    const iconeOlho = document.getElementById('icone-olho');

    if (inputSenha.type === 'password') {
        inputSenha.type = 'text';
        // Troca para o ícone de olho cortado (fechado)
        iconeOlho.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>';
    } else {
        inputSenha.type = 'password';
        // Volta para o ícone de olho normal (aberto)
        iconeOlho.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
    }
}