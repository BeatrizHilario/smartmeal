// ==========================================================================
// 1. CONFIGURAÇÕES E ESTADOS GERAIS
// ==========================================================================
const HOJE = new Date().toLocaleDateString();

// Controle de virada de dia (Limpa cache se for um dia novo)
if (localStorage.getItem("smartmeal_data") !== HOJE) {
    localStorage.clear();
    localStorage.setItem("smartmeal_data", HOJE);
    localStorage.setItem("agua_consumida", "0");
}

// Inicialização das Cores do Tailwind no Frontend
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
// 2. LÓGICA DE REFEIÇÕES E IA DA TELA PRINCIPAL (SUGESTÃO INTELIGENTE)
// ==========================================================================
document.addEventListener("DOMContentLoaded", () => {

    // Configurações do componente de Perfil Incompleto
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

    // Eventos das abas do Perfil Incompleto
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

        // Define a aba correta baseada no cache
        mealBtns.forEach(b => {
            b.classList.replace("bg-verdeSalvia", "bg-gray-100");
            b.classList.replace("text-white", "text-textoClaro");
            if (b.getAttribute("data-tipo") === tipoAtual) {
                b.classList.replace("bg-gray-100", "bg-verdeSalvia");
                b.classList.replace("text-textoClaro", "text-white");
            }
        });

        // Carrega o conteúdo do cache se existir
        const cacheKeyIncial = "dieta_" + tipoAtual;
        if (localStorage.getItem(cacheKeyIncial)) {
            containerDieta.innerHTML = localStorage.getItem(cacheKeyIncial);
        } else if (containerDieta.innerHTML.trim() !== "") {
            localStorage.setItem(cacheKeyIncial, containerDieta.innerHTML);
        }

        // Função assíncrona de busca na API
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
                containerDieta.innerHTML = "<p class='text-red-500 font-bold text-center w-full'>Erro de comunicação com a IA.</p>";
            } finally {
                containerDieta.style.opacity = "1";
                if (btnGerarOutra) btnGerarOutra.disabled = false;
            }
        }

        // Ações de clique nas abas
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
// 4. SISTEMA DE MODAIS E OBSERVERS DA UI
// ==========================================================================

function abrirModal(id) {
    const modal = document.getElementById(id);
    if(modal) {
        modal.classList.remove('hidden');
        setTimeout(() => {
            modal.classList.remove('opacity-0');
            modal.querySelector('div').classList.remove('scale-95');
        }, 10);
    }
}

function fecharModal(id) {
    const modal = document.getElementById(id);
    if(modal) {
        modal.classList.add('opacity-0');
        modal.querySelector('div').classList.add('scale-95');
        setTimeout(() => modal.classList.add('hidden'), 300);
    }
}

// Injeção dinâmica do Botão de Consumo na tela da IA
function injetarBotaoConsumo() {
    const container = document.getElementById("container-dieta-ia");
    if (!container) return;

    // A classe \[\#FDF7DF\] é a responsável pelo card amarelo vindo da API
    const cardKcal = container.querySelector(".bg-\\[\\#FDF7DF\\]");

    if (cardKcal && !cardKcal.querySelector(".btn-consumir-sugestao")) {
        const linhaDivisoria = document.createElement("div");
        linhaDivisoria.className = "w-full border-t border-dashed border-amareloMostarda/40 my-3";

        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "btn-consumir-sugestao";
        btn.innerHTML = '<i class="fa-solid fa-circle-check"></i> Consumi esta Sugestão!';
        btn.onclick = window.consumirSugestaoIA;

        cardKcal.appendChild(linhaDivisoria);
        cardKcal.appendChild(btn);
    }
}

window.consumirSugestaoIA = function() {
    const container = document.getElementById("container-dieta-ia");
    if (!container) return;

    const tituloEl = container.querySelector("h4");
    const kcalEl = container.querySelector("p.text-3xl");

    if (!tituloEl || !kcalEl) {
        alert("Gere uma sugestão primeiro!");
        return;
    }

    const botaoAtivo = document.querySelector(".meal-btn.bg-verdeSalvia");

    document.getElementById("input-sug-tipo").value = botaoAtivo ? botaoAtivo.getAttribute("data-tipo") : "Sugerida";
    document.getElementById("input-sug-desc").value = tituloEl.innerText.trim();
    document.getElementById("input-sug-kcal").value = parseInt(kcalEl.innerText.replace(/\D/g, '')) || 0;

    document.getElementById("form-sugestao-ia").submit();
};

// Listeners Globais da UI
document.addEventListener("DOMContentLoaded", () => {

    // Tratamento de botões de abertura de modal
    document.querySelectorAll('[id^="btn-abrir-"]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const alvo = btn.id.replace('btn-abrir-', 'modal-');
            abrirModal(alvo);
        });
    });

    // Tratamento de fechamento em cliques externos e menus suspensos
    document.addEventListener('click', (e) => {
        const dropdown = document.getElementById('dropdown-perfil');
        const btnAvatar = document.getElementById('btn-avatar-perfil');
        if (dropdown && btnAvatar && !btnAvatar.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.add('hidden');
        }

        ['modal-completar-perfil', 'modal-editar-perfil', 'modal-visualizar-dados', 'modal-editar-agua'].forEach(id => {
            const modal = document.getElementById(id);
            if(modal && e.target === modal) fecharModal(id);
        });
    });

    // Injeção do Mutation Observer para a área da IA (Reinjeta botões após carregamento)
    const containerDieta = document.getElementById("container-dieta-ia");
    if (containerDieta) {
        injetarBotaoConsumo();
        new MutationObserver(injetarBotaoConsumo).observe(containerDieta, { childList: true, subtree: true });
    }

    // Tela de Carregamento (Spinner)
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

    // Lógica do botão flutuante Varinha Mágica no Mobile
    const btnFabGerar = document.getElementById("btn-fab-gerar");
    if (btnFabGerar) {
        btnFabGerar.addEventListener("click", (e) => {
            e.preventDefault();
            if ("vibrate" in navigator) navigator.vibrate([50, 100, 50]);

            const icone = btnFabGerar.querySelector('i');
            icone.classList.add('fa-spin');
            setTimeout(() => icone.classList.remove('fa-spin'), 1000);

            const btn = document.getElementById("btn-gerar-outras") || document.getElementById("btn-gerar-sugestao");
            if(btn) btn.click();
        });
    }
});