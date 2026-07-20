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
            Você é um nutricionista especialista em IA e orçamento familiar.
            Gere UMA ÚNICA sugestão de %s personalizada para o paciente abaixo.
            
            -Nome: %s
            -Idade: %d anos
            -Estado: %s (use isso para entender a variação regional de preços de alimentos)
            -Peso Atual: %.2f kg
            -Altura: %d cm
            -Objetivo Principal: %s
            -Orçamento Máximo Mensal para Alimentação: R$ %.2f
            
            Regras escritas de negócio:
                1. PROIBIDO gerar cardápios diários ou semanais. Gere APENAS UMA única sugestão de %s.
                2. O custo estimado dos ingredientes deve respeitar o orçamento informado.
                3. Priorize alimentos sazonais e acessíveis.
                4. Responda APENAS usando marcação HTML limpa (tags <h3>, <ul>, <li>, <p>, <b>).\s
                5. PROIBIDO USAR FORMATO JSON. Não coloque tags <html> ou <body>, apenas o conteúdo formatado.
                6. Não dê justificativas nem faça introduções, apenas escreva a sugestão de refeição. 
                7. SEJA EXTREMAMENTE DIRETO. Responda APENAS com o nome do prato e a lista de ingredientes com quantidades.
                8. PROIBIDO escrever introduções, conclusões, saudações ou justificativas.
                9. Você DEVE retornar EXATAMENTE o bloco de código HTML abaixo, substituindo APENAS as informações entre colchetes [ ] pelos dados calculados da refeição:
                
                                   <div class="flex flex-col md:flex-row items-start justify-between gap-6">
                                       <div class="flex-1 w-full text-textoClaro font-medium text-sm leading-relaxed max-h-[400px] overflow-y-auto pr-4">
                                           <p class="font-bold text-textoEscuro mb-3 text-base text-amareloMostarda">[NOME DO PRATO]</p>
                                           <ul class="list-disc pl-5 space-y-1 mb-4">
                                               <li>[QUANTIDADE] de [INGREDIENTE]</li>
                                               <li>[QUANTIDADE] de [INGREDIENTE]</li>
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
                tipoRefeicao,
                usuario.getNome() != null ? usuario.getNome() : "Sem nome",
                usuario.getIdade() != null ? usuario.getIdade() : 0,
                usuario.getEstado() != null ? usuario.getEstado() : "Não informado",
                usuario.getPesoKg() != null ? usuario.getPesoKg().doubleValue() : 0.0,
                usuario.getAlturaCm() != null ? usuario.getAlturaCm() : 0,
                usuario.getObjetivo() != null ? usuario.getObjetivo() : "Saúde geral",
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

        /*Map<String, Object> config = new HashMap<>();
        config.put("responseMimeType", "application/json");
        requestBody.put("generationConfig", config);*/

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

            return (String) firstPart.get("text");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Isso vai imprimir o erro REAL do Google no seu terminal (em vermelho)
            System.err.println("=== ERRO DETALHADO DO GOOGLE ===");
            System.err.println(e.getResponseBodyAsString());
            System.err.println("================================");
            throw new RuntimeException("O Google rejeitou a requisição: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha crítica ao comunicar com a API do Gemini: " + e.getMessage());
        }
    }

    public String gerarIdeiasEconomicasDoDia(String tipoRefeicao) {

        if(tipoRefeicao == null || tipoRefeicao.trim().isEmpty()) {
            tipoRefeicao = "Café da Manhã";
        }

        String prompt = "Atue como um chef de cozinha e nutricionista muito criativo focado em economia doméstica. "
                + "Gere UMA ÚNICA sugestão de " + tipoRefeicao + " saudável e de baixo custo. "
                + "REGRAS OBRIGATÓRIAS: "
                + "1. SEJA ALEATÓRIO E VARIE MUITO. Fuja do óbvio. "
                + "2. PROIBIDO gerar listas com várias opções. Gere APENAS uma sugestão por vez. "
                + "3. Nunca sugira apenas 'salada genérica' ou dependa sempre de grão-de-bico."
                + "4. PROIBIDO USAR FORMATO JSON."
                + "5. PROIBIDO escrever introduções, conclusões, saudações ou justificativas.\n"
                + "6. Você DEVE retornar EXATAMENTE o bloco de código HTML abaixo, substituindo APENAS as informações entre colchetes [ ] pelos dados calculados da refeição:\n"
                + "\n"
                + "                                   <div class=\"bg-white border border-gray-100 rounded-3xl shadow-sm overflow-hidden\">\n"
                + "                                       <div class=\"bg-verdeSalvia/10 px-6 py-4 flex flex-col md:flex-row justify-between items-center gap-4\">\n"
                + "                                           <h4 class=\"font-bold text-lg text-verdeSalvia text-center md:text-left w-full md:w-auto\">[NOME DO PRATO]</h4>\n"
                + "                                           <div class=\"bg-white px-4 py-2 rounded-2xl shadow-sm border border-verdeSalvia/20 whitespace-nowrap\">\n"
                + "                                               <span class=\"text-sm text-textoClaro font-semibold mr-1\">Total:</span>\n"
                + "                                               <span class=\"text-xl font-bold text-verdeSalvia\">[XXX] kcal</span>\n"
                + "                                           </div>\n"
                + "                                       </div>\n"
                + "                                       <div class=\"p-6\">\n"
                + "                                           <p class=\"text-sm font-bold text-textoEscuro mb-3\"><i class=\"fa-solid fa-basket-shopping mr-2 text-amareloMostarda\"></i>Ingredientes:</p>\n"
                + "                                           <ul class=\"list-disc pl-5 mb-6 text-sm text-textoClaro md:columns-2 gap-8 space-y-2 md:space-y-0\">\n"
                + "                                               <li class=\"mb-2\">[QUANTIDADE] de [INGREDIENTE]</li>\n"
                + "                                               <li class=\"mb-2\">[QUANTIDADE] de [INGREDIENTE]</li>\n"
                + "                                           </ul>\n"
                + "                                           <div class=\"flex flex-wrap gap-3 pt-4 border-t border-gray-100\">\n"
                + "                                               <span class=\"bg-red-50 text-red-600 px-3 py-1.5 rounded-lg text-xs font-bold flex items-center\"><div class=\"w-2 h-2 rounded-full bg-red-500 mr-2\"></div> [X]g Proteína</span>\n"
                + "                                               <span class=\"bg-blue-50 text-blue-600 px-3 py-1.5 rounded-lg text-xs font-bold flex items-center\"><div class=\"w-2 h-2 rounded-full bg-blue-500 mr-2\"></div> [X]g Carbo</span>\n"
                + "                                               <span class=\"bg-yellow-50 text-yellow-700 px-3 py-1.5 rounded-lg text-xs font-bold flex items-center\"><div class=\"w-2 h-2 rounded-full bg-yellow-500 mr-2\"></div> [X]g Gordura</span>\n"
                + "                                           </div>\n"
                + "                                       </div>\n"
                + "                                   </div> ";

        // Replicando a sua estrutura exata de mapas para a API do Google
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

            // Destrinchando a resposta usando o seu exato algoritmo das linhas 78-83
            List candidates = (List) response.getBody().get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List responseParts = (List) content.get("parts");
            Map firstPart = (Map) responseParts.get(0);

            String respostaGemini = (String) firstPart.get("text");
            respostaGemini = respostaGemini.replace("```html", "")
                    .replace("```", "")
                    .trim();
            return respostaGemini;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("=== ERRO DETALHADO DO GOOGLE NA SUGESTAO ===");
            System.err.println(e.getResponseBodyAsString());
            return getReceitaFallback();
        } catch (Exception e) {
            e.printStackTrace();
            return getReceitaFallback();
        }
    }

    // Método auxiliar (adicione isso no final da classe, antes da última chave })
    private String getReceitaFallback() {
        return """
            <!-- Aviso de IA Cansada -->
            <div class="bg-orange-50 border-l-4 border-orange-400 p-3 mb-5 rounded-r-xl shadow-sm">
                <p class="text-xs text-orange-700 font-bold"><i class="fa-solid fa-triangle-exclamation mr-1"></i> A IA precisa de um fôlego!</p>
                <p class="text-xs text-orange-600 mt-1">Devido ao alto volume de testes, atingimos o limite de buscas. Enquanto ela descansa uns minutinhos, preparamos esta receita clássica para você:</p>
            </div>
        
            <!-- Receita Curinga Fixa -->
            <div class="bg-white border border-gray-100 rounded-3xl shadow-sm overflow-hidden">
                <div class="bg-verdeSalvia/10 px-6 py-4 flex flex-col md:flex-row justify-between items-center gap-4">
                    <h4 class="font-bold text-lg text-verdeSalvia text-center md:text-left w-full md:w-auto">Crepioca Proteica de Frango</h4>
                    <div class="bg-white px-4 py-2 rounded-2xl shadow-sm border border-verdeSalvia/20 whitespace-nowrap">
                        <span class="text-sm text-textoClaro font-semibold mr-1">Total:</span>
                        <span class="text-xl font-bold text-verdeSalvia">280 kcal</span>
                    </div>
                </div>
                <div class="p-6">
                    <p class="text-sm font-bold text-textoEscuro mb-3"><i class="fa-solid fa-basket-shopping mr-2 text-amareloMostarda"></i>Ingredientes:</p>
                    <ul class="list-disc pl-5 mb-6 text-sm text-textoClaro md:columns-2 gap-8 space-y-2 md:space-y-0 marker:text-amareloMostarda">
                        <li class="mb-2">2 colheres de sopa de goma de tapioca</li>
                        <li class="mb-2">1 ovo inteiro</li>
                        <li class="mb-2">3 colheres de sopa de frango desfiado já pronto</li>
                        <li class="mb-2">1 colher de chá de azeite</li>
                        <li class="mb-2">Sal e orégano a gosto</li>
                    </ul>
                    <div class="flex flex-wrap gap-3 pt-4 border-t border-gray-100">
                        <span class="bg-red-50 text-red-600 px-3 py-1.5 rounded-lg text-xs font-bold flex items-center"><div class="w-2 h-2 rounded-full bg-red-500 mr-2"></div> 22g Proteína</span>
                        <span class="bg-blue-50 text-blue-600 px-3 py-1.5 rounded-lg text-xs font-bold flex items-center"><div class="w-2 h-2 rounded-full bg-blue-500 mr-2"></div> 18g Carbo</span>
                        <span class="bg-yellow-50 text-yellow-700 px-3 py-1.5 rounded-lg text-xs font-bold flex items-center"><div class="w-2 h-2 rounded-full bg-yellow-500 mr-2"></div> 10g Gordura</span>
                    </div>
                </div>
            </div>
            """;
    }
}
