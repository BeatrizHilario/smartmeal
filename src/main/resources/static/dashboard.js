// ==========================================================================
// 1. CONFIGURAÇÕES E ESTADOS GERAIS
// ==========================================================================
const HOJE = new Date().toLocaleDateString();

if (localStorage.getItem("smartmeal_data") !== HOJE) {
    localStorage.clear();
    localStorage.setItem("smartmeal_data", HOJE);
    localStorage.setItem("agua_consumida", "0");
}

tailwind.config = {
    theme: {
        extend: {
            colors: {
                fundoCreme: '#FDF7DF',
                verdeSalvia: '#84A98C',
                verdeEscuro: '#52796F',
                amareloMostarda: '#F4B41A',
                amareloHover: '#E0A10D',
                textoEscuro: '#4A4A4A',
                textoClaro: '#7A7A7A',
                cardBranco: '#FFFFFF'
            },
            fontFamily: {
                sans: ['Poppins', 'sans-serif'],
                cursiva: ['Rancho', 'cursive']
            }
        }
    }
};

// ==========================================================================
// 2. LÓGICA DE REFEIÇÕES E IA (SUGESTÃO INTELIGENTE E PERFIL INCOMPLETO)
// ==========================================================================
document.addEventListener("DOMContentLoaded", () => {

    // --- Lógica para Perfil Incompleto ---
    const btnGerarOutras = document.getElementById("btn-gerar-outras");
    const containerTextoIncompleto = document.getElementById("texto-ideitas-ia");
    const mealBtnsIncompleto = document.querySelectorAll(".meal-btn-incompleto");
    let tipoRefeicaoIncompleto = "Café da Manhã";

    async function buscarIdeiasIncompleto(tipo) {
        if (!containerTextoIncompleto) return;
        containerTextoIncompleto.style.opacity = "0.5";
        containerTextoIncompleto.innerHTML = `
            <div class="flex flex-col items-center justify-center py-10">
                <i class="fa-solid fa-spinner fa-spin text-3xl text-verdeSalvia mb-3"></i>
                <p>A IA está pensando em seu ${tipo}...</p>
            </div>`;
        if (btnGerarOutras) btnGerarOutras.disabled = true;

        try {
            const resposta = await fetch(`/api/receitas/aleatorias?tipoRefeicao=${encodeURIComponent(tipo)}`);
            containerTextoIncompleto.innerHTML = await resposta.text();
        } catch (error) {
            containerTextoIncompleto.innerHTML = "<p class='text-red-500 font-bold'>Falha ao conectar com a IA. Tente novamente.</p>";
        } finally {
            containerTextoIncompleto.style.opacity = "1";
            if (btnGerarOutras) btnGerarOutras.disabled = false;
        }
    }

    mealBtnsIncompleto.forEach(btn => {
        btn.addEventListener("click", (e) => {
            mealBtnsIncompleto.forEach(b => {
                b.classList.replace("bg-verdeSalvia", "bg-gray-100");
                b.classList.replace("text-white", "text-textoClaro");
            });
            e.target.classList.replace("bg-gray-100", "bg-verdeSalvia");
            e.target.classList.replace("text-textoClaro", "text-white");
            tipoRefeicaoIncompleto = e.target.getAttribute("data-tipo");
            buscarIdeiasIncompleto(tipoRefeicaoIncompleto);
        });
    });

    if (btnGerarOutras) {
        btnGerarOutras.addEventListener("click", () => buscarIdeiasIncompleto(tipoRefeicaoIncompleto));
    }

    // --- Sugestão Inteligente (Perfil Completo) ---
    const mealBtns = document.querySelectorAll(".meal-btn");
    const btnGerarOutra = document.getElementById("btn-gerar-sugestao");
    const containerDieta = document.getElementById("container-dieta-ia");

    if (containerDieta && mealBtns.length > 0) {
        let tipoAtual = localStorage.getItem("ultima_aba_refeicao") || "Café da Manhã";

        mealBtns.forEach(b => {
            b.classList.replace("bg-verdeSalvia", "bg-gray-100");
            b.classList.replace("text-white", "text-textoClaro");
            if (b.getAttribute("data-tipo") === tipoAtual) {
                b.classList.replace("bg-gray-100", "bg-verdeSalvia");
                b.classList.replace("text-textoClaro", "text-white");
            }
        });

        const cacheKeyIncial = "dieta_" + tipoAtual;
        if (localStorage.getItem(cacheKeyIncial)) {
            containerDieta.innerHTML = localStorage.getItem(cacheKeyIncial);
        } else if (containerDieta.innerHTML.trim() !== "") {
            localStorage.setItem(cacheKeyIncial, containerDieta.innerHTML);
        }

        async function buscarSugestaoIA(tipo, forcarNova = false) {
            const cacheKey = "dieta_" + tipo;

            if (!forcarNova && localStorage.getItem(cacheKey)) {
                containerDieta.innerHTML = localStorage.getItem(cacheKey);
                return;
            }

            containerDieta.style.opacity = "0.5";
            containerDieta.innerHTML = `
                <div class="flex flex-col items-center justify-center w-full py-10">
                    <i class="fa-solid fa-spinner fa-spin text-3xl text-verdeSalvia mb-3"></i>
                    <p class="text-sm font-medium text-textoClaro">A IA está pensando em seu ${tipo}...</p>
                </div>`;
            if (btnGerarOutra) btnGerarOutra.disabled = true;

            try {
                const formData = new URLSearchParams();
                formData.append("tipoRefeicao", tipo);

                const resposta = await fetch("/api/dieta/nova-sugestao", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: formData
                });

                const htmlGerado = await resposta.text();
                localStorage.setItem(cacheKey, htmlGerado);
                containerDieta.innerHTML = htmlGerado;
            } catch (erro) {
                containerDieta.innerHTML = "<p class='text-red-500 font-bold text-center w-full'>Erro ao comunicar com a IA.</p>";
            } finally {
                containerDieta.style.opacity = "1";
                if (btnGerarOutra) btnGerarOutra.disabled = false;
            }
        }

        mealBtns.forEach(btn => {
            btn.addEventListener("click", (e) => {
                mealBtns.forEach(b => {
                    b.classList.replace("bg-verdeSalvia", "bg-gray-100");
                    b.classList.replace("text-white", "text-textoClaro");
                });
                e.target.classList.replace("bg-gray-100", "bg-verdeSalvia");
                e.target.classList.replace("text-textoClaro", "text-white");

                tipoAtual = e.target.getAttribute("data-tipo");
                localStorage.setItem("ultima_aba_refeicao", tipoAtual);
                buscarSugestaoIA(tipoAtual);
            });
        });

        if (btnGerarOutra) {
            btnGerarOutra.addEventListener("click", () => buscarSugestaoIA(tipoAtual, true));
        }
    }
});

// ==========================================================================
// 3. LÓGICA DE HIDRATAÇÃO DINÂMICA
// ==========================================================================
document.addEventListener('DOMContentLoaded', () => {
    const card = document.getElementById('card-hidratacao');
    if(!card) return;

    const metaAgua = parseInt(card.getAttribute('data-meta')) || 2000;
    let aguaConsumida = parseInt(localStorage.getItem("agua_consumida")) || 0;
    let ultimoConsumo = parseInt(localStorage.getItem("agua_ultimo_registro")) || 0;

    const elFaltante = document.getElementById('texto-agua-faltante');
    const elConsumida = document.getElementById('texto-agua-consumida');
    const containerCopos = document.getElementById('container-copos');

    const formatarVolume = (ml) => ml >= 1000 ? (ml / 1000).toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) + ' L' : ml + ' ml';

    const renderizarAgua = () => {
        elFaltante.textContent = formatarVolume(Math.max(0, metaAgua - aguaConsumida));
        elConsumida.textContent = formatarVolume(aguaConsumida);
        containerCopos.innerHTML = '';

        for (let i = 1; i <= Math.ceil(metaAgua / 500); i++) {
            const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            svg.setAttribute("viewBox", "0 0 24 24");
            svg.setAttribute("class", `w-8 h-10 cursor-pointer transition-all duration-300 hover:scale-110 hover:-translate-y-1 ${aguaConsumida >= i * 500 ? "text-[#4CB5F9] drop-shadow-md" : "text-gray-200"}`);
            svg.innerHTML = '<path fill="currentColor" d="M4 2h16l-2 20H6L4 2zm2.2 2l1.6 16h8.4l1.6-16H6.2z"/>';

            svg.onclick = () => {
                if (aguaConsumida >= i * 500) {
                    aguaConsumida = Math.max(0, aguaConsumida - 500);
                    ultimoConsumo = 0;
                } else {
                    aguaConsumida += 500;
                    ultimoConsumo = 500;
                }
                localStorage.setItem("agua_consumida", aguaConsumida);
                localStorage.setItem("agua_ultimo_registro", ultimoConsumo);
                renderizarAgua();
            };
            containerCopos.appendChild(svg);
        }
    };

    window.editarAguaManual = () => {
        document.getElementById('input-agua-manual').value = ultimoConsumo > 0 ? ultimoConsumo : '';
        abrirModal('modal-editar-agua');
    };

    window.salvarAguaManual = () => {
        const novoValor = parseInt(document.getElementById('input-agua-manual').value.replace(/\D/g, ''));
        if (!isNaN(novoValor)) {
            aguaConsumida = Math.max(0, aguaConsumida - ultimoConsumo + novoValor);
            ultimoConsumo = novoValor;
            localStorage.setItem("agua_consumida", aguaConsumida);
            localStorage.setItem("agua_ultimo_registro", ultimoConsumo);
            renderizarAgua();
        }
        fecharModal('modal-editar-agua');
    };

    window.zerarAgua = () => {
        aguaConsumida = ultimoConsumo = 0;
        localStorage.setItem("agua_consumida", 0);
        localStorage.setItem("agua_ultimo_registro", 0);
        renderizarAgua();
        fecharModal('modal-editar-agua');
    };

    renderizarAgua();
});

// ==========================================================================
// 4. SISTEMA DE MODAIS E INJEÇÃO DE BOTÕES
// ==========================================================================

window.abrirModal = function(id) {
    const modal = document.getElementById(id);
    if (!modal) return;

    modal.classList.remove('hidden');
    setTimeout(() => {
        modal.classList.remove('opacity-0');
        const container = modal.querySelector('div');
        if (container) container.classList.remove('scale-95');
    }, 10);
};

window.fecharModal = function(id) {
    const modal = document.getElementById(id);
    if (!modal) return;

    modal.classList.add('opacity-0');
    const container = modal.querySelector('div');
    if (container) container.classList.add('scale-95');
    setTimeout(() => modal.classList.add('hidden'), 300);
};

document.addEventListener("DOMContentLoaded", () => {
    // Abertura do modal de Configurações (Desktop e Mobile)
    const btnEditarDesktop = document.getElementById("btn-abrir-editar-perfil");
    const btnEditarMobile = document.getElementById("btn-abrir-editar-perfil-mobile");
    const btnCompletar = document.getElementById("btn-completar-perfil");

    if (btnEditarDesktop) {
        btnEditarDesktop.addEventListener("click", (e) => {
            e.preventDefault();
            abrirModal("modal-editar-perfil");
        });
    }

    if (btnEditarMobile) {
        btnEditarMobile.addEventListener("click", (e) => {
            e.preventDefault();
            abrirModal("modal-editar-perfil");
        });
    }

    if (btnCompletar) {
        btnCompletar.addEventListener("click", (e) => {
            e.preventDefault();
            abrirModal("modal-completar-perfil");
        });
    }

    // Fechamento ao clicar fora do modal
    const modais = ['modal-completar-perfil', 'modal-editar-perfil', 'modal-visualizar-dados', 'modal-editar-agua'];
    modais.forEach(id => {
        const modal = document.getElementById(id);
        if (modal) {
            modal.addEventListener("click", (e) => {
                if (e.target === modal) fecharModal(id);
            });
        }
    });

    // Dropdown do Avatar
    const dropdown = document.getElementById('dropdown-perfil');
    const btnAvatar = document.getElementById('btn-avatar-perfil');
    document.addEventListener('click', (e) => {
        if (dropdown && btnAvatar && !btnAvatar.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.add('hidden');
        }
    });

    // Injeção do Botão de Consumo na IA
    const containerDieta = document.getElementById("container-dieta-ia");
    if (containerDieta) {
        injetarBotaoConsumo();
        new MutationObserver(injetarBotaoConsumo).observe(containerDieta, { childList: true, subtree: true });
    }

    // Loader Global
    const loader = document.getElementById("loader-global");
    if (loader) {
        document.addEventListener("submit", () => {
            loader.classList.remove("hidden");
            loader.classList.add("flex");
        });
        window.addEventListener("pageshow", () => {
            loader.classList.add("hidden");
            loader.classList.remove("flex");
        });
    }
});

// Intercepta recursos bloqueados para perfil incompleto
window.bloquearRecursoIncompleto = function() {
    alert("Complete o perfil para desbloquear esse recurso!");
    abrirModal("modal-completar-perfil");
};

// Funções globais de modal
window.abrirModal = function(id) {
    const modal = document.getElementById(id);
    if (!modal) return;

    modal.classList.remove('hidden');
    setTimeout(() => {
        modal.classList.remove('opacity-0');
        const container = modal.querySelector('div');
        if (container) container.classList.remove('scale-95');
    }, 10);
};

window.fecharModal = function(id) {
    const modal = document.getElementById(id);
    if (!modal) return;

    modal.classList.add('opacity-0');
    const container = modal.querySelector('div');
    if (container) container.classList.add('scale-95');
    setTimeout(() => modal.classList.add('hidden'), 300);
};