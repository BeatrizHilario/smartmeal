// === CONTROLE DE VIRADA DE DIA ===
const HOJE = new Date().toLocaleDateString();

// Se a data salva for diferente de hoje, ele limpa o cache antigo
if (localStorage.getItem("smartmeal_data") !== HOJE) {
    localStorage.clear(); // Limpa as dietas salvas de ontem
    localStorage.setItem("smartmeal_data", HOJE);
    localStorage.setItem("agua_consumida", "0");
}

// Configuração do Tailwind (Cores e Fontes Personalizadas)
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

// Função do botão de Inteligência Artificial para registrar refeição
function registrarRefeicao() {
    const btn = document.getElementById('btn-registrar');
    const kcalEl = document.getElementById('kcal-atual');
    const gastoEl = document.getElementById('gasto-atual');

    if(btn.innerText !== "Consumido!") {
        btn.innerHTML = '<i class="fa-solid fa-check mr-2"></i> Adicionado ao Diário!';
        btn.classList.remove('bg-amareloMostarda', 'hover:bg-amareloHover');
        btn.classList.add('bg-verdeSalvia');

        kcalEl.innerText = "1.900";
        gastoEl.innerText = "R$ 193,50";

        // Reseta o botão após 3 segundos
        setTimeout(() => {
            btn.innerHTML = 'Registrar Consumo no Diário';
            btn.classList.remove('bg-verdeSalvia');
            btn.classList.add('bg-amareloMostarda', 'hover:bg-amareloHover');
        }, 3000);
    }
}

// Função para interação com os copos de água (Antiga, mantida por compatibilidade se usada em outro lugar)
function beberAgua(elemento) {
    elemento.classList.remove('text-gray-200');
    elemento.classList.add('text-verdeSalvia');
    elemento.onclick = null;
}

// --- LÓGICA DE REFEIÇÕES PARA PERFIL INCOMPLETO ---
document.addEventListener("DOMContentLoaded", () => {
    const btnGerarOutras = document.getElementById("btn-gerar-outras");
    const containerTexto = document.getElementById("texto-ideitas-ia");
    const mealBtnsIncompleto = document.querySelectorAll(".meal-btn-incompleto");

    let tipoRefeicaoIncompleto = "Café da Manhã";

    async function buscarIdeiasIncompleto(tipo) {
        if (!containerTexto) return;

        containerTexto.style.opacity = "0.5";
        containerTexto.innerHTML = `<div class="flex flex-col items-center justify-center py-10">
            <i class="fa-solid fa-spinner fa-spin text-3xl text-verdeSalvia mb-3"></i>
            <p>A IA está pensando em seu ${tipo}...</p>
        </div>`;
        if (btnGerarOutras) btnGerarOutras.disabled = true;

        try {
            const resposta = await fetch(`/api/receitas/aleatorias?tipoRefeicao=${encodeURIComponent(tipo)}`);
            const novoTexto = await resposta.text();
            containerTexto.innerHTML = novoTexto;
        } catch (error) {
            console.error("Erro ao buscar novas receitas:", error);
            containerTexto.innerHTML = "<p class='text-red-500 font-bold'>Houve uma falha ao conectar com a IA. Tente novamente.</p>";
        } finally {
            containerTexto.style.opacity = "1";
            if (btnGerarOutras) btnGerarOutras.disabled = false;
        }
    }

    mealBtnsIncompleto.forEach(btn => {
        btn.addEventListener("click", (e) => {
            mealBtnsIncompleto.forEach(b => {
                b.classList.remove("bg-verdeSalvia", "text-white", "shadow-md");
                b.classList.add("bg-gray-100", "text-textoClaro");
            });

            const botaoClicado = e.target;
            botaoClicado.classList.remove("bg-gray-100", "text-textoClaro");
            botaoClicado.classList.add("bg-verdeSalvia", "text-white", "shadow-md");

            tipoRefeicaoIncompleto = botaoClicado.getAttribute("data-tipo");
            buscarIdeiasIncompleto(tipoRefeicaoIncompleto);
        });
    });

    if (btnGerarOutras) {
        btnGerarOutras.addEventListener("click", () => {
            buscarIdeiasIncompleto(tipoRefeicaoIncompleto);
        });
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const btnAbrirPerfil = document.getElementById("btn-completar-perfil");
    const modalPerfil = document.getElementById("modal-completar-perfil");
    const btnFecharPerfil = document.getElementById("btn-fechar-perfil");

    if (btnAbrirPerfil && modalPerfil) {
        btnAbrirPerfil.addEventListener("click", () => {
            modalPerfil.classList.remove("hidden");
            setTimeout(() => {
                modalPerfil.classList.remove("opacity-0");
                modalPerfil.querySelector('div').classList.remove("scale-95");
            }, 10);
        });

        const fecharModal = () => {
            modalPerfil.classList.add("opacity-0");
            modalPerfil.querySelector('div').classList.add("scale-95");
            setTimeout(() => modalPerfil.classList.add("hidden"), 300);
        };

        btnFecharPerfil.addEventListener("click", fecharModal);
        modalPerfil.addEventListener("click", (e) => {
            if (e.target === modalPerfil) fecharModal();
        });
    }
});

// --- LÓGICA INTELIGENTE DE SUGESTÃO DA IA COM CACHE ---
document.addEventListener("DOMContentLoaded", () => {
    const mealBtns = document.querySelectorAll(".meal-btn");
    const btnGerarOutra = document.getElementById("btn-gerar-sugestao");
    const containerDieta = document.getElementById("container-dieta-ia");

    if (!containerDieta || mealBtns.length === 0) return;

    // 1. Recupera qual era a última aba que o usuário estava vendo (ou o padrão)
    let tipoAtual = localStorage.getItem("ultima_aba_refeicao") || "Café da Manhã";

    // 2. Arruma as cores dos botões assim que a página carrega
    mealBtns.forEach(b => {
        b.classList.remove("bg-verdeSalvia", "text-white", "shadow-md");
        b.classList.add("bg-gray-100", "text-textoClaro");
        if (b.getAttribute("data-tipo") === tipoAtual) {
            b.classList.remove("bg-gray-100", "text-textoClaro");
            b.classList.add("bg-verdeSalvia", "text-white", "shadow-md");
        }
    });

    // 3. Força a tela a mostrar o que está na memória
    const cacheKeyIncial = "dieta_" + tipoAtual;
    if (localStorage.getItem(cacheKeyIncial)) {
        containerDieta.innerHTML = localStorage.getItem(cacheKeyIncial);
    } else {
        // Se for a primeira vez no dia, salva o que veio do Java na memória
        const htmlBackend = containerDieta.innerHTML;
        if (htmlBackend && htmlBackend.trim() !== "") {
            localStorage.setItem(cacheKeyIncial, htmlBackend);
        }
    }

    // Função que verifica o cache antes de incomodar a IA
    async function buscarSugestaoIA(tipo, forcarNova = false) {
        const cacheKey = "dieta_" + tipo;

        // Se NÃO forçou nova receita e já existe no cache, carrega instantâneo!
        if (!forcarNova && localStorage.getItem(cacheKey)) {
            containerDieta.innerHTML = localStorage.getItem(cacheKey);
            return;
        }

        // Se não tem no cache, mostra a animação de carregamento
        containerDieta.style.opacity = "0.5";
        containerDieta.innerHTML = `<div class="flex flex-col items-center justify-center py-10">
            <i class="fa-solid fa-spinner fa-spin text-3xl text-verdeSalvia mb-3"></i>
            <p>A IA está pensando em seu ${tipo}...</p>
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

            // Salva a resposta na memória do dia e joga na tela
            localStorage.setItem(cacheKey, htmlGerado);
            containerDieta.innerHTML = htmlGerado;
        } catch (erro) {
            containerDieta.innerHTML = "<p class='text-red-500 font-bold'>Erro ao comunicar com a IA. Tente novamente.</p>";
            console.error(erro);
        } finally {
            containerDieta.style.opacity = "1";
            if (btnGerarOutra) btnGerarOutra.disabled = false;
        }
    }

    // Clique nos botões de refeição
    mealBtns.forEach(btn => {
        btn.addEventListener("click", (e) => {
            mealBtns.forEach(b => {
                b.classList.remove("bg-verdeSalvia", "text-white", "shadow-md");
                b.classList.add("bg-gray-100", "text-textoClaro");
            });

            const botaoClicado = e.target;
            botaoClicado.classList.remove("bg-gray-100", "text-textoClaro");
            botaoClicado.classList.add("bg-verdeSalvia", "text-white", "shadow-md");

            tipoAtual = botaoClicado.getAttribute("data-tipo");
            localStorage.setItem("ultima_aba_refeicao", tipoAtual); // <--- Memoriza a aba!

            buscarSugestaoIA(tipoAtual);
        });
    });

    if (btnGerarOutra) {
        btnGerarOutra.addEventListener("click", () => {
            buscarSugestaoIA(tipoAtual, true);
        });
    }
});

// --- LÓGICA DE HIDRATAÇÃO DINÂMICA (EDIÇÃO DO ÚLTIMO COPO) ---
document.addEventListener('DOMContentLoaded', () => {
    const card = document.getElementById('card-hidratacao');
    if(!card) return;

    const metaAgua = parseInt(card.getAttribute('data-meta')) || 2000;

    // Inicializa recuperando da memória o total E o último copo
    let aguaConsumida = parseInt(localStorage.getItem("agua_consumida")) || 0;
    let ultimoConsumo = parseInt(localStorage.getItem("agua_ultimo_registro")) || 0;

    const elFaltante = document.getElementById('texto-agua-faltante');
    const elConsumida = document.getElementById('texto-agua-consumida');
    const containerCopos = document.getElementById('container-copos');

    const formatarVolume = (ml) => {
        if (ml >= 1000) {
            return (ml / 1000).toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) + ' L';
        }
        return ml + ' ml';
    };

    const renderizarAgua = () => {
        let faltam = metaAgua - aguaConsumida;
        if (faltam < 0) faltam = 0;

        elFaltante.textContent = formatarVolume(faltam);
        elConsumida.textContent = formatarVolume(aguaConsumida);

        const totalCopos = Math.ceil(metaAgua / 500);
        containerCopos.innerHTML = '';

        for (let i = 1; i <= totalCopos; i++) {
            const copoValor = i * 500;

            const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            svg.setAttribute("viewBox", "0 0 24 24");
            svg.setAttribute("class", "w-8 h-10 cursor-pointer transition-all duration-300 hover:scale-110 hover:-translate-y-1");

            if (aguaConsumida >= copoValor) {
                svg.classList.add("text-[#4CB5F9]", "drop-shadow-md");
            } else {
                svg.classList.add("text-gray-200");
            }

            svg.innerHTML = '<path fill="currentColor" d="M4 2h16l-2 20H6L4 2zm2.2 2l1.6 16h8.4l1.6-16H6.2z"/>';

            // Lógica do clique na tela principal
            svg.onclick = () => {
                if (aguaConsumida >= copoValor) {
                    // Remover água desmarcando o copo
                    aguaConsumida = Math.max(0, aguaConsumida - 500);
                    ultimoConsumo = 0; // Se desmarcou, quebra a corrente de edição
                } else {
                    // Adicionou um copo novo
                    aguaConsumida += 500;
                    ultimoConsumo = 500; // Memoriza que o último copo teve 500ml
                }

                localStorage.setItem("agua_consumida", aguaConsumida);
                localStorage.setItem("agua_ultimo_registro", ultimoConsumo);
                renderizarAgua();
            };

            containerCopos.appendChild(svg);
        }
    };

    // Abre o modal preenchido com o valor do último copo!
    window.editarAguaManual = () => {
        const modal = document.getElementById('modal-editar-agua');
        const input = document.getElementById('input-agua-manual');

        input.value = ultimoConsumo > 0 ? ultimoConsumo : '';

        modal.classList.remove('hidden');
        setTimeout(() => {
            modal.classList.remove('opacity-0');
            modal.querySelector('div').classList.remove('scale-95');
        }, 10);
    };

    window.fecharModalAgua = () => {
        const modal = document.getElementById('modal-editar-agua');
        modal.classList.add('opacity-0');
        modal.querySelector('div').classList.add('scale-95');
        setTimeout(() => modal.classList.add('hidden'), 300);
    };

    // A MÁGICA: Substitui o valor do último copo sem mexer no resto!
    window.salvarAguaManual = () => {
        const input = document.getElementById('input-agua-manual');
        const novoValorEditado = parseInt(input.value.replace(/\D/g, ''));

        if (!isNaN(novoValorEditado)) {
            // Conta mágica: Pega o total, arranca fora o último copo e bota o valor novo!
            aguaConsumida = aguaConsumida - ultimoConsumo + novoValorEditado;
            if (aguaConsumida < 0) aguaConsumida = 0;

            ultimoConsumo = novoValorEditado; // Atualiza a memória

            localStorage.setItem("agua_consumida", aguaConsumida);
            localStorage.setItem("agua_ultimo_registro", ultimoConsumo);
            renderizarAgua();
        }
        fecharModalAgua();
    };

    // Botão de emergência para limpar resíduos
    window.zerarAgua = () => {
        aguaConsumida = 0;
        ultimoConsumo = 0;
        localStorage.setItem("agua_consumida", 0);
        localStorage.setItem("agua_ultimo_registro", 0);
        renderizarAgua();
        fecharModalAgua();
    };

    renderizarAgua();
});

// --- LÓGICA DO MODAL DE EDITAR PERFIL ---
document.addEventListener("DOMContentLoaded", () => {
    const btnAbrirEditar = document.getElementById("btn-abrir-editar-perfil");
    const btnAbrirEditarMobile = document.getElementById("btn-abrir-editar-perfil-mobile");

    const modalEditar = document.getElementById("modal-editar-perfil");
    const btnFecharEditar = document.getElementById("btn-fechar-editar-perfil");

    if (modalEditar) {
        const abrirModal = (e) => {
            e.preventDefault();
            modalEditar.classList.remove("hidden");
            setTimeout(() => {
                modalEditar.classList.remove("opacity-0");
                modalEditar.querySelector('div').classList.remove("scale-95");
            }, 10);
        };

        if (btnAbrirEditar) btnAbrirEditar.addEventListener("click", abrirModal);
        if (btnAbrirEditarMobile) btnAbrirEditarMobile.addEventListener("click", abrirModal);

        const fecharModalEditar = () => {
            modalEditar.classList.add("opacity-0");
            modalEditar.querySelector('div').classList.add("scale-95");
            setTimeout(() => modalEditar.classList.add("hidden"), 300);
        };

        if (btnFecharEditar) btnFecharEditar.addEventListener("click", fecharModalEditar);
        modalEditar.addEventListener("click", (e) => {
            if (e.target === modalEditar) fecharModalEditar();
        });
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const modalEmBreve = document.getElementById("modal-em-breve");
    const btnFecharEmBreve = document.getElementById("btn-fechar-em-breve");
    const linksBloqueados = document.querySelectorAll('.link-em-breve');

    linksBloqueados.forEach(link => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            modalEmBreve.classList.remove("hidden");
            setTimeout(() => {
                modalEmBreve.classList.remove("opacity-0");
                modalEmBreve.querySelector('div').classList.remove("scale-95");
            }, 10);
        });
    });

    if (btnFecharEmBreve) {
        btnFecharEmBreve.addEventListener("click", () => {
            modalEmBreve.classList.add("opacity-0");
            modalEmBreve.querySelector('div').classList.add("scale-95");
            setTimeout(() => modalEmBreve.classList.add("hidden"), 300);
        });
    }
});

// --- LÓGICA DO BOTÃO CENTRAL (VARINHA MÁGICA) ---
document.addEventListener("DOMContentLoaded", () => {
    const btnFabGerar = document.getElementById("btn-fab-gerar");
    const btnGerarOutras = document.getElementById("btn-gerar-outras");
    const btnGerarSugestao = document.getElementById("btn-gerar-sugestao");

    if (btnFabGerar) {
        btnFabGerar.addEventListener("click", (e) => {
            e.preventDefault();

            if ("vibrate" in navigator) {
                navigator.vibrate([50, 100, 50]);
            }

            const icone = btnFabGerar.querySelector('i');
            icone.classList.add('fa-spin');
            setTimeout(() => icone.classList.remove('fa-spin'), 1000);

            if (btnGerarOutras) {
                btnGerarOutras.click();
            } else if (btnGerarSugestao) {
                btnGerarSugestao.click();
            }
        });
    }
});

document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('dropdown-perfil');
    const btnAvatar = document.getElementById('btn-avatar-perfil');

    if (dropdown && btnAvatar && !btnAvatar.contains(event.target) && !dropdown.contains(event.target)) {
        dropdown.classList.add('hidden');
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const mainElement = document.querySelector('main');
    const modais = ['modal-completar-perfil', 'modal-editar-perfil', 'modal-visualizar-dados', 'modal-em-breve'];

    const observer = new MutationObserver(() => {
    const modalAberto = modais.some(id => {
        const el = document.getElementById(id);
        return el && !el.classList.contains('hidden');
    });

    if (modalAberto) {
        mainElement.classList.remove('overflow-y-auto');
        mainElement.classList.add('overflow-y-hidden');
        } else {
            mainElement.classList.remove('overflow-y-hidden');
            mainElement.classList.add('overflow-y-auto');
            }
    });

    modais.forEach(id => {
    const el = document.getElementById(id);
        if (el) {
            observer.observe(el, { attributes: true, attributeFilter: ['class'] });
        }
    });
});

// Envia a sugestão atual direto para o banco
function consumirSugestaoIA() {
    const container = document.getElementById("container-dieta-ia");

    const tituloEl = container.querySelector("p.text-base") || container.querySelector("h4");
    const kcalEl = container.querySelector("p.text-3xl") || container.querySelector("span.text-xl");

    if (!tituloEl || !kcalEl) {
        alert("Gere uma sugestão primeiro!");
        return;
    }

    const descricaoPrato = tituloEl.innerText.trim();
    const caloriasNum = parseInt(kcalEl.innerText.replace(/\D/g, '')) || 0;

    const botaoAtivo = document.querySelector(".meal-btn.bg-verdeSalvia");
    const tipo = botaoAtivo ? botaoAtivo.getAttribute("data-tipo") : "Refeição Sugerida";

    document.getElementById("input-sug-tipo").value = tipo;
    document.getElementById("input-sug-desc").value = descricaoPrato;
    document.getElementById("input-sug-kcal").value = caloriasNum;

    document.getElementById("form-sugestao-ia").submit();
}

// --- SISTEMA DE TRANSIÇÃO SUAVE (APENAS PARA PROCESSAMENTO PESADO) ---
document.addEventListener("DOMContentLoaded", () => {
    const loader = document.getElementById("loader-global");
    if (!loader) return;

    // O loader animado agora SÓ vai aparecer quando você enviar um formulário pesado (ex: IA ou Salvar Perfil)
    document.addEventListener("submit", () => {
        loader.classList.remove("hidden");
        loader.classList.add("flex");
    });

    // Garante que o loader suma caso o usuário clique em "Voltar" no navegador
    window.addEventListener("pageshow", () => {
        loader.classList.add("hidden");
        loader.classList.remove("flex");
    });
});