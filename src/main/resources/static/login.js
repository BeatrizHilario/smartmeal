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

