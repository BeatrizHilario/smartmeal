package br.com.smartmeal.smartmeal.service;

import br.com.smartmeal.smartmeal.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ArtificialIntelligenceService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String gerarDietaPelaIA(Usuario usuario, String tipoRefeicao) {

        String instrucaoPrompt = """
            Você é um chef de cozinha prático e nutricionista focado em culinária funcional, saborosa e acessível para o dia a dia brasileiro.
            DATA DE HOJE: %s.
            Gere UMA ÚNICA sugestão de %s personalizada para o paciente abaixo.
            
            - Nome: %s
            - Idade: %d anos
            - Estado: %s (variação regional de preços de alimentos)
            - Peso Atual: %.2f kg
            - Altura: %d cm
            - Objetivo Principal: %s
            - Restrições Alimentares: %s
            - Orçamento Máximo Mensal para Alimentação: R$ %.2f
            
            REGRAS DE OURO DE NEGÓCIO E CULINÁRIA:
            1. EQUILÍBRIO ENTRE PRATICIDADE E SABOR: Fuja do óbvio sem complicar a vida do usuário. Sugira preparos rápidos de frigideira, grelha ou montagem simples (5 a 15 min), mas que sejam atraentes usando temperos inteligentes (ex: páprica defumada, orégano, raspas de limão, alho-poró, cúrcuma, ervas finas, requeijão light, queijo minas).
            2. PROIBIDO RECEITAS COMPLEXAS: Nada de tortas demoradas de forno, assados longos, suflês ou receitas que exijam múltiplos processos. O usuário precisa de algo fácil para o dia a dia.
            3. RESTRIÇÃO ABSOLUTA: É estritamente PROIBIDO utilizar ou recomendar qualquer ingrediente, derivado ou alimento listado em '- Restrições Alimentares'.
            4. ORÇAMENTO E SAZONALIDADE: Respeite o orçamento e priorize itens frescos e acessíveis de supermercado e feira local.
            5. REGRA CULTURAL RIGOROSA: Se for 'Café da Manhã', mantenha o contexto matinal brasileiro (pães integrais, torradas, ovos mexidos/cremosos, queijos brancos, tapiocas, crepiocas, iogurtes, frutas, café com leite). PROIBIDO frango, carne moída, bifes, peixes ou legumes cozidos no café da manhã.
            6. PROIBIDO ADJETIVOS: Não adicione adjetivos floreados ao nome do prato (ex: proibido 'delicioso', 'maravilhoso', 'supremo'). Foque no preparo e ingredientes (ex: 'Ovos Cremosos com Páprica no Pão Tostado e Requeijão Light', 'Filé de Frango Selado com Ervas, Arroz e Tomate Confit').
            7. FORMATO E ESTRUTURA: Retorne APENAS HTML limpo (sem JSON, sem markdown ```html, sem saudações ou explicações).
            8. Você DEVE retornar EXATAMENTE o bloco HTML abaixo, substituindo os dados entre colchetes [ ]:
                
            <div class="flex flex-col md:flex-row items-start justify-between gap-6">
                <div class="flex-1 w-full text-textoClaro font-medium text-sm leading-relaxed max-h-[400px] overflow-y-auto pr-4">
                    <p class="font-bold text-textoEscuro mb-3 text-base text-amareloMostarda">[NOME DO PRATO]</p>
                    <ul class="list-disc pl-5 space-y-1 mb-4">
                        <li>[QUANTIDADE] de [INGREDIENTE]</li>
                        <li>[QUANTIDADE] de [INGREDIENTE COM TEMPERO OU TOQUE ESPECIAL]</li>
                    </ul>
                    <div class="flex flex-wrap gap-4 mt-6 text-xs font-bold text-textoEscuro border-t border-verdeSalvia/20 pt-4">
                        <span class="flex items-center"><div class="w-2 h-2 rounded-full bg-red-400 mr-1.5"></div> [X]g Proteína</span>
                        <span class="flex items-center"><div class="w-2 h-2 rounded-full bg-blue-400 mr-1.5"></div> [X]g Carbo</span>
                        <span class="flex items-center"><div class="w-2 h-2 rounded-full bg-yellow-400 mr-1.5"></div> [X]g Gordura</span>
                    </div>
                </div>
                <div class="bg-fundoCreme border-2 border-dashed border-amareloMostarda rounded-3xl p-6 text-center w-full md:w-48 shrink-0 mt-2">
                    <p class="text-sm text-textoClaro font-semibold mb-1">Total da Refeição</p>
                    <p class="text-3xl font-bold text-amareloMostarda mb-2">[XXX] kcal</p>
                </div>
            </div>
            """.formatted(
                java.time.LocalDate.now().toString(),
                tipoRefeicao,
                usuario.getNome() != null ? usuario.getNome() : "Sem nome",
                usuario.getIdade() != null ? usuario.getIdade() : 0,
                usuario.getEstado() != null ? usuario.getEstado() : "Não informado",
                usuario.getPesoKg() != null ? usuario.getPesoKg().doubleValue() : 0.0,
                usuario.getAlturaCm() != null ? usuario.getAlturaCm() : 0,
                usuario.getObjetivo() != null ? usuario.getObjetivo() : "Saúde geral",
                (usuario.getRestricaoAlimentar() != null && !usuario.getRestricaoAlimentar().trim().isEmpty()) ? usuario.getRestricaoAlimentar() : "Nenhuma",
                usuario.getOrcamentoMaxMensal() != null ? usuario.getOrcamentoMaxMensal().doubleValue() : 0.0,
                tipoRefeicao
        );

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", instrucaoPrompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> contents = new HashMap<>();
        contents.put("contents", Collections.singletonList(parts));

        requestBody.put("contents", contents.get("contents"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String urlCompleta = apiUrl + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(urlCompleta, entity, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List responseParts = (List) content.get("parts");
            Map firstPart = (Map) responseParts.get(0);

            return ((String) firstPart.get("text")).replace("```html", "").replace("```", "").trim();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha crítica ao comunicar com a API do Gemini: " + e.getMessage());
        }
    }

    public String gerarIdeiasEconomicasDoDia(String tipoRefeicao) {
        if (tipoRefeicao == null || tipoRefeicao.trim().isEmpty()) {
            tipoRefeicao = "Café da Manhã";
        }

        String prompt = """
            Você é um cozinheiro criativo e nutricionista focado em culinária prática, saborosa e acessível para o dia a dia brasileiro.
            Gere UMA ÚNICA sugestão de %s que seja rápida de fazer (5 a 15 minutos), econômica, de baixas a moderadas calorias e com combinações inteligentes que saiam do óbvio.

            DIRETRIZES DE CRIATIVIDADE E SABOR:
            1. ELEVE O PRÁTICO: Transforme ingredientes básicos usando toques simples de sabor (ex: páprica defumada, orégano, cúrcuma, raspas de limão, alho-poró picado, tomate cereja tostado, queijo minas, requeijão light).
            2. PREPARO RÁPIDO: Deve ser feito na frigideira, grelha ou montagem rápida. PROIBIDO receitas longas de forno, suflês ou massas complexas.
            3. REGRA CULTURAL RIGOROSA: Se for 'Café da Manhã', mantenha itens tradicionais matinais brasileiros (pães integrais, torradas, ovos mexidos, tapioca, crepioca, queijos leves, aveia, frutas). PROIBIDO frango desfiado, carnes vermelhas ou legumes cozidos de almoço.
            4. PROIBIDO ADJETIVOS: Não use termos como 'delicioso' ou 'maravilhoso'. Nome direto: ex. 'Pão Tostado com Ovos Mexidos, Páprica e Requeijão Light'.
            5. APENAS UMA SUGESTÃO com 3 a 5 ingredientes listados com quantidades exatas.
            6. PROIBIDO explicações, introduções ou blocos ```html.
            7. Retorne ESTRITAMENTE a estrutura HTML abaixo, substituindo os campos entre colchetes [ ]:

            <div class="flex flex-col md:flex-row items-start justify-between gap-6">
                <div class="flex-1 w-full text-textoClaro font-medium text-sm leading-relaxed max-h-[400px] overflow-y-auto pr-4">
                    <h4 class="font-bold text-lg text-verdeEscuro mb-3">[NOME DO PRATO]</h4>
                    <p class="text-xs font-bold text-textoClaro uppercase tracking-wider mb-2">Ingredientes:</p>
                    <ul class="list-disc pl-5 space-y-1.5 text-sm text-textoEscuro font-medium">
                        <li>[QUANTIDADE] de [INGREDIENTE]</li>
                        <li>[QUANTIDADE] de [INGREDIENTE]</li>
                        <li>[QUANTIDADE] de [INGREDIENTE COM TEMPERO OU TOQUE ESPECIAL]</li>
                    </ul>
                </div>
                <div class="bg-fundoCreme border-2 border-dashed border-amareloMostarda rounded-3xl p-5 text-center w-full md:w-44 shrink-0 mt-2">
                    <p class="text-xs text-textoClaro font-semibold mb-1">Total Estimado</p>
                    <p class="text-2xl font-bold text-amareloMostarda">[XXX] kcal</p>
                </div>
            </div>
            """.formatted(tipoRefeicao);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> contents = new HashMap<>();
        contents.put("contents", Collections.singletonList(parts));

        requestBody.put("contents", contents.get("contents"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String urlCompleta = apiUrl + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(urlCompleta, entity, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List responseParts = (List) content.get("parts");
            Map firstPart = (Map) responseParts.get(0);

            String respostaGemini = (String) firstPart.get("text");
            return respostaGemini.replace("```html", "").replace("```", "").trim();
        } catch (Exception e) {
            System.err.println("Fallback acionado para ideias do dia: " + e.getMessage());
            return obterSugestaoPreDefinidaComAviso(tipoRefeicao);
        }
    }

    /**
     * Sorteia pratos práticos pré-definidos com alerta visual de IA sobrecarregada.
     */
    private String obterSugestaoPreDefinidaComAviso(String tipoRefeicao) {
        String avisoIa = """
            <div class="mb-3 px-3 py-1.5 rounded-xl bg-amber-50 border border-amber-200 text-amber-700 text-xs font-semibold flex items-center gap-2">
                <i class="fa-solid fa-triangle-exclamation text-amber-500"></i>
                <span>A IA está ocupada no momento. Selecionamos uma receita prática do nosso acervo do dia para você:</span>
            </div>
        """;

        String tipoNorm = tipoRefeicao.toLowerCase();
        List<String[]> opcoes = new ArrayList<>();

        if (tipoNorm.contains("café") || tipoNorm.contains("cafe")) {
            opcoes.add(new String[]{
                    "Pão Tostado com Ovos Mexidos, Páprica e Requeijão Light",
                    "280 kcal",
                    "<li>2 ovos inteiros</li><li>1 fatia de pão francês ou integral</li><li>1 colher de sopa de requeijão light</li><li>1 pitada de páprica defumada e orégano</li>"
            });
            opcoes.add(new String[]{
                    "Crepioca Rápida com Queijo Minas e Orégano",
                    "260 kcal",
                    "<li>1 ovo inteiro</li><li>2 colheres de sopa de goma de tapioca</li><li>1 fatia média de queijo minas frescal</li><li>1 pitada de orégano tostado na frigideira</li>"
            });
            opcoes.add(new String[]{
                    "Cuscuz de Frigideira com Queijo Branco e Café com Leite",
                    "310 kcal",
                    "<li>3 colheres de sopa de flocão de milho hidratado</li><li>1 fatia de queijo branco</li><li>1 xícara de café com leite desnatado</li>"
            });
        } else if (tipoNorm.contains("almoço") || tipoNorm.contains("almoco")) {
            opcoes.add(new String[]{
                    "Frango Grelhado com Alho, Arroz Branco e Cenoura Ralada",
                    "410 kcal",
                    "<li>120g de filé de peito de frango temperado com alho e limão</li><li>4 colheres de sopa de arroz branco cozido</li><li>1 concha pequena de feijão carioca</li><li>Salada de cenoura ralada com azeite e vinagre</li>"
            });
            opcoes.add(new String[]{
                    "Carne Moída Refogada com Cúrcuma, Arroz e Couve Rasgada",
                    "430 kcal",
                    "<li>100g de patinho moído refogado com alho e cúrcuma</li><li>4 colheres de sopa de arroz branco</li><li>1 pires de couve refogada no azeite</li>"
            });
            opcoes.add(new String[]{
                    "Omelete Recheada de Queijo e Tomate com Arroz e Feijão",
                    "390 kcal",
                    "<li>2 ovos inteiros batidos com cheiro-verde</li><li>1 fatia de queijo picado com tomate</li><li>3 colheres de sopa de arroz e meia concha de feijão</li>"
            });
        } else if (tipoNorm.contains("lanche")) {
            opcoes.add(new String[]{
                    "Iogurte Natural com Banana, Aveia e Canela",
                    "210 kcal",
                    "<li>1 pote de iogurte natural desnatado (160g)</li><li>1 banana média em rodelas</li><li>1 colher de sopa de farelo de aveia</li><li>1 pitada de canela em pó</li>"
            });
            opcoes.add(new String[]{
                    "Torrada Integral com Pasta de Ricota e Manjericão",
                    "190 kcal",
                    "<li>2 torradas integrais</li><li>2 colheres de sopa de ricota amassada com azeite</li><li>Folhas frescas de manjericão e orégano</li>"
            });
        } else {
            // Jantar
            opcoes.add(new String[]{
                    "Filé de Frango em Cubos com Brócolis e Mandioca Cozida",
                    "350 kcal",
                    "<li>110g de frango em cubos selado na frigideira com páprica</li><li>1 xícara de brócolis cozido ao vapor</li><li>2 pedaços pequenos de mandioca cozida</li>"
            });
            opcoes.add(new String[]{
                    "Ovos Poché sobre Cama de Tomates Refogados com Ervas",
                    "270 kcal",
                    "<li>2 ovos cozidos com gema mole</li><li>2 tomates picados refogados com alho e azeite</li><li>1 fatia de pão tostado para acompanhar</li>"
            });
        }

        int indiceAleatorio = new Random().nextInt(opcoes.size());
        String[] prato = opcoes.get(indiceAleatorio);

        return """
            %s
            <div class="flex flex-col md:flex-row items-start justify-between gap-6">
                <div class="flex-1 w-full text-textoClaro font-medium text-sm leading-relaxed max-h-[400px] overflow-y-auto pr-4">
                    <h4 class="font-bold text-lg text-verdeEscuro mb-3">%s</h4>
                    <p class="text-xs font-bold text-textoClaro uppercase tracking-wider mb-2">Ingredientes:</p>
                    <ul class="list-disc pl-5 space-y-1.5 text-sm text-textoEscuro font-medium">
                        %s
                    </ul>
                </div>
                <div class="bg-fundoCreme border-2 border-dashed border-amareloMostarda rounded-3xl p-5 text-center w-full md:w-44 shrink-0 mt-2">
                    <p class="text-xs text-textoClaro font-semibold mb-1">Total Estimado</p>
                    <p class="text-2xl font-bold text-amareloMostarda">%s</p>
                </div>
            </div>
        """.formatted(avisoIa, prato[0], prato[2], prato[1]);
    }

    public Integer estimarCaloriasRefeicaoLivre(String descricao) {
        String prompt = String.format("Atue como nutricionista. Estime o total aproximado de calorias para a seguinte refeição: '%s'. Retorne APENAS o número inteiro de calorias, sem textos adicionais, sem formatação, sem a palavra 'kcal'.", descricao);

        try {
            String respostaIA = chamarApiGemini(prompt);
            return Integer.parseInt(respostaIA.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            System.err.println("Erro ao estimar calorias via IA: " + e.getMessage());
            return 0;
        }
    }

    public String chamarApiGemini(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> contents = new HashMap<>();
        contents.put("contents", Collections.singletonList(parts));

        requestBody.put("contents", contents.get("contents"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String urlCompleta = apiUrl + "?key=" + apiKey;
        ResponseEntity<Map> response = restTemplate.postForEntity(urlCompleta, entity, Map.class);

        List candidates = (List) response.getBody().get("candidates");
        Map firstCandidate = (Map) candidates.get(0);
        Map content = (Map) firstCandidate.get("content");
        List responseParts = (List) content.get("parts");
        Map firstPart = (Map) responseParts.get(0);

        return (String) firstPart.get("text");
    }
}