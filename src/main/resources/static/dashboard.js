// Configuração do Tailwind (Cores e Fontes Personalizadas)
tailwind.config = {
    theme: {
        extend: {
            colors: {
                fundoCreme: '#FDF7DF', /* Cor de fundo creme da sua imagem */
                verdeSalvia: '#84A98C', /* Verde do card e das ondas */
                verdeEscuro: '#52796F',
                amareloMostarda: '#F4B41A', /* Amarelo do botão de login e da logo */
                amareloHover: '#E0A10D',
                textoEscuro: '#4A4A4A',
                textoClaro: '#7A7A7A',
                cardBranco: '#FFFFFF'
            },
            fontFamily: {
                sans: ['Poppins', 'sans-serif'],
                cursiva: ['Rancho', 'cursive'] /* Fonte estilo a da sua logo */
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

// Função para interação com os copos de água
function beberAgua(elemento) {
    elemento.classList.remove('text-gray-200');
    elemento.classList.add('text-verdeSalvia');
    // Remove o evento de clique após o copo ser selecionado
    elemento.onclick = null;
}

// --- LÓGICA DE REFEIÇÕES PARA PERFIL INCOMPLETO ---
document.addEventListener("DOMContentLoaded", () => {
    const btnGerarOutras = document.getElementById("btn-gerar-outras");
    const containerTexto = document.getElementById("texto-ideitas-ia");
    const mealBtnsIncompleto = document.querySelectorAll(".meal-btn-incompleto");

    let tipoRefeicaoIncompleto = "Café da Manhã"; // Estado inicial

    async function buscarIdeiasIncompleto(tipo) {
        if (!containerTexto) return;

        // Efeito visual de carregamento
        containerTexto.style.opacity = "0.5";
        containerTexto.innerHTML = `<div class="flex flex-col items-center justify-center py-10">
            <i class="fa-solid fa-spinner fa-spin text-3xl text-verdeSalvia mb-3"></i>
            <p>A IA está pensando em seu ${tipo}...</p>
        </div>`;
        if (btnGerarOutras) btnGerarOutras.disabled = true;

        try {
            // Chamada com parâmetro para dizer à IA qual refeição queremos
            const resposta = await fetch(`/api/receitas/aleatorias?tipoRefeicao=${encodeURIComponent(tipo)}`);
            const novoTexto = await resposta.text();

            // Injeta usando innerHTML para ler os cards visuais perfeitos
            containerTexto.innerHTML = novoTexto;
        } catch (error) {
            console.error("Erro ao buscar novas receitas:", error);
            containerTexto.innerHTML = "<p class='text-red-500 font-bold'>Houve uma falha ao conectar com a IA. Tente novamente.</p>";
        } finally {
            containerTexto.style.opacity = "1";
            if (btnGerarOutras) btnGerarOutras.disabled = false;
        }
    }

    // Escuta o clique nas pílulas do perfil incompleto
    mealBtnsIncompleto.forEach(btn => {
        btn.addEventListener("click", (e) => {
            // Remove a cor verde de todos
            mealBtnsIncompleto.forEach(b => {
                b.classList.remove("bg-verdeSalvia", "text-white", "shadow-md");
                b.classList.add("bg-gray-100", "text-textoClaro");
            });

            // Ativa o clicado
            const botaoClicado = e.target;
            botaoClicado.classList.remove("bg-gray-100", "text-textoClaro");
            botaoClicado.classList.add("bg-verdeSalvia", "text-white", "shadow-md");

            tipoRefeicaoIncompleto = botaoClicado.getAttribute("data-tipo");
            buscarIdeiasIncompleto(tipoRefeicaoIncompleto);
        });
    });

    // Escuta o clique no "Gerar Outra"
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
        // Função para Abrir
        btnAbrirPerfil.addEventListener("click", () => {
            modalPerfil.classList.remove("hidden");
            // Um mini-atraso para o navegador processar a animação de opacidade
            setTimeout(() => {
                modalPerfil.classList.remove("opacity-0");
                modalPerfil.querySelector('div').classList.remove("scale-95");
            }, 10);
        });

        // Função de Fechar Genérica
        const fecharModal = () => {
            modalPerfil.classList.add("opacity-0");
            modalPerfil.querySelector('div').classList.add("scale-95");
            setTimeout(() => modalPerfil.classList.add("hidden"), 300); // Espera a animação acabar
        };

        // Escuta o clique no "X" e no fundo preto
        btnFecharPerfil.addEventListener("click", fecharModal);
        modalPerfil.addEventListener("click", (e) => {
            if (e.target === modalPerfil) fecharModal();
        });
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const mealBtns = document.querySelectorAll(".meal-btn");
    const btnGerarOutra = document.getElementById("btn-gerar-sugestao");
    const containerDieta = document.getElementById("container-dieta-ia");
    const tituloCard = document.getElementById("titulo-refeicao-ia");

    let tipoAtual = "Café da Manhã"; // Estado inicial

    // 1. Função que fala com o Java para pedir o cardápio
    async function buscarSugestaoIA(tipo) {
        // Efeito visual de carregamento
        containerDieta.style.opacity = "0.5";
        containerDieta.innerHTML = `<div class="flex flex-col items-center justify-center py-10">
            <i class="fa-solid fa-spinner fa-spin text-3xl text-verdeSalvia mb-3"></i>
            <p>A IA está pensando em seu ${tipo}...</p>
        </div>`;
        btnGerarOutra.disabled = true;

        try {
            // Chama a rota que criámos no Controller usando POST
            const formData = new URLSearchParams();
            formData.append("tipoRefeicao", tipo);

            const resposta = await fetch("/api/dieta/nova-sugestao", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: formData
            });

            const htmlGerado = await resposta.text();

            // Injeta a nova dieta perfeitamente formatada
            containerDieta.innerHTML = htmlGerado;
        } catch (erro) {
            containerDieta.innerHTML = "<p class='text-red-500 font-bold'>Erro ao comunicar com a IA. Tente novamente.</p>";
            console.error(erro);
        } finally {
            containerDieta.style.opacity = "1";
            btnGerarOutra.disabled = false;
        }
    }

    // 2. Escuta o clique nos botões em formato de pílula
    mealBtns.forEach(btn => {
        btn.addEventListener("click", (e) => {
            // Remove a cor verde de todos os botões e deixa cinzento
            mealBtns.forEach(b => {
                b.classList.remove("bg-verdeSalvia", "text-white", "shadow-md");
                b.classList.add("bg-gray-100", "text-textoClaro");
            });

            // Pinta apenas o botão que foi clicado de verde
            const botaoClicado = e.target;
            botaoClicado.classList.remove("bg-gray-100", "text-textoClaro");
            botaoClicado.classList.add("bg-verdeSalvia", "text-white", "shadow-md");

            // Atualiza a variável e dispara a IA
            tipoAtual = botaoClicado.getAttribute("data-tipo");
            buscarSugestaoIA(tipoAtual);
        });
    });

    // 3. O botão "Gerar Outra" também pede uma nova ideia do tipo selecionado
    if (btnGerarOutra) {
        btnGerarOutra.addEventListener("click", () => {
            buscarSugestaoIA(tipoAtual);
        });
    }
});

// --- LÓGICA DE HIDRATAÇÃO DINÂMICA (Copos de 500ml e Edição) ---
document.addEventListener('DOMContentLoaded', () => {
    const card = document.getElementById('card-hidratacao');
    if(!card) return;

    // Pega a meta do usuário que veio do Java (padrão 2000 caso dê erro)
    const metaAgua = parseInt(card.getAttribute('data-meta')) || 2000;
    let aguaConsumida = 0;

    const elFaltante = document.getElementById('texto-agua-faltante');
    const elConsumida = document.getElementById('texto-agua-consumida');
    const containerCopos = document.getElementById('container-copos');

    // Função mágica que converte ml para Litros se for >= 1000
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

        // Calcula quantos copos de 500ml a meta exige
        const totalCopos = Math.ceil(metaAgua / 500);
        containerCopos.innerHTML = '';

        for (let i = 1; i <= totalCopos; i++) {
            const copoValor = i * 500;

            const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            svg.setAttribute("viewBox", "0 0 24 24");
            svg.setAttribute("class", "w-8 h-10 cursor-pointer transition-all duration-300 hover:scale-110 hover:-translate-y-1");

            // Pinta de azul se a água já alcançou esse copo
            if (aguaConsumida >= copoValor) {
                svg.classList.add("text-[#4CB5F9]", "drop-shadow-md");
            } else {
                svg.classList.add("text-gray-200");
            }

            svg.innerHTML = '<path fill="currentColor" d="M4 2h16l-2 20H6L4 2zm2.2 2l1.6 16h8.4l1.6-16H6.2z"/>';

            svg.onclick = () => {
                if (aguaConsumida === copoValor) {
                    aguaConsumida = copoValor - 500;
                } else {
                    aguaConsumida = copoValor;
                }
                renderizarAgua();
            };

            containerCopos.appendChild(svg);
        }
    };

    // Função para abrir o novo modal bonitinho
        window.editarAguaManual = () => {
            const modal = document.getElementById('modal-editar-agua');
            const input = document.getElementById('input-agua-manual');

            input.value = aguaConsumida; // Preenche com o valor atual

            modal.classList.remove('hidden');
            setTimeout(() => {
                modal.classList.remove('opacity-0');
                modal.querySelector('div').classList.remove('scale-95');
            }, 10);
        };

        // Função para fechar com animação
        window.fecharModalAgua = () => {
            const modal = document.getElementById('modal-editar-agua');
            modal.classList.add('opacity-0');
            modal.querySelector('div').classList.add('scale-95');
            setTimeout(() => modal.classList.add('hidden'), 300);
        };

        // Função para ler o input e salvar
        window.salvarAguaManual = () => {
            const input = document.getElementById('input-agua-manual');
            const valor = parseInt(input.value.replace(/\D/g, '')); // Aceita só números

            if (!isNaN(valor)) {
                aguaConsumida = valor;
                renderizarAgua(); // Re-desenha os copos e os litros
            }
            fecharModalAgua();
        };

    // Roda a primeira vez para desenhar a tela
    renderizarAgua();
});

// --- LÓGICA DO MODAL DE EDITAR PERFIL ---
document.addEventListener("DOMContentLoaded", () => {
    // Pegamos os dois botões (Desktop e Mobile)
    const btnAbrirEditar = document.getElementById("btn-abrir-editar-perfil");
    const btnAbrirEditarMobile = document.getElementById("btn-abrir-editar-perfil-mobile");

    const modalEditar = document.getElementById("modal-editar-perfil");
    const btnFecharEditar = document.getElementById("btn-fechar-editar-perfil");

    if (modalEditar) {
        // Criamos uma função única para abrir, que servirá para ambos os botões
        const abrirModal = (e) => {
            e.preventDefault();
            modalEditar.classList.remove("hidden");
            setTimeout(() => {
                modalEditar.classList.remove("opacity-0");
                modalEditar.querySelector('div').classList.remove("scale-95");
            }, 10);
        };

        // Adicionamos o evento de clique aos botões, caso eles existam na tela
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

    // Selecionamos os links do Diário Alimentar e Orçamento (adicione IDs ou classes neles se necessário)
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

    // Pegamos os botões originais (mesmo que estejam escondidos no mobile)
    const btnGerarOutras = document.getElementById("btn-gerar-outras"); // Perfil Incompleto
    const btnGerarSugestao = document.getElementById("btn-gerar-sugestao"); // Perfil Completo

    if (btnFabGerar) {
        btnFabGerar.addEventListener("click", (e) => {
            e.preventDefault();

            // 🪄 1. Efeito Tátil (Vibração no celular)
            // O padrão [50, 100, 50] significa: vibra 50ms, pausa 100ms, vibra 50ms (como uma faísca mágica!)
            if ("vibrate" in navigator) {
                navigator.vibrate([50, 100, 50]);
            }

            // 2. Efeito visual: faz a varinha girar rapidamente
            const icone = btnFabGerar.querySelector('i');
            icone.classList.add('fa-spin');
            setTimeout(() => icone.classList.remove('fa-spin'), 1000);

            // 3. Aciona o botão correto dependendo de qual tela o usuário está
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

    // Se o clique não foi no botão E não foi dentro do menu, esconde o menu
    if (dropdown && btnAvatar && !btnAvatar.contains(event.target) && !dropdown.contains(event.target)) {
        dropdown.classList.add('hidden');
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const mainElement = document.querySelector('main');
    // Lista com os IDs de todos os modais da sua tela
    const modais = ['modal-completar-perfil', 'modal-editar-perfil', 'modal-visualizar-dados', 'modal-em-breve'];

    // Cria um 'olheiro' (Observer) que detecta quando as classes mudam
    const observer = new MutationObserver(() => {
    // Verifica se tem pelo menos UM modal aberto (sem a classe 'hidden')
    const modalAberto = modais.some(id => {
        const el = document.getElementById(id);
        return el && !el.classList.contains('hidden');
    });

    if (modalAberto) {
        // Trava o fundo
        mainElement.classList.remove('overflow-y-auto');
        mainElement.classList.add('overflow-y-hidden');
        } else {
            // Libera o fundo
            mainElement.classList.remove('overflow-y-hidden');
            mainElement.classList.add('overflow-y-auto');
            }
    });

    // Coloca o 'olheiro' para vigiar a classe de todos os modais da lista
    modais.forEach(id => {
    const el = document.getElementById(id);
        if (el) {
            observer.observe(el, { attributes: true, attributeFilter: ['class'] });
        }
    });
});