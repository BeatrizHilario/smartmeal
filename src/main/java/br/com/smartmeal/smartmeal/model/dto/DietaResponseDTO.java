package br.com.smartmeal.smartmeal.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DietaResponseDTO {

    private String id;
    private Integer idUsuario;
    private LocalDateTime dataGeracao;
    private Double custoEstimado;
    private PlanoAlimentarDTO planoAlimentar;
    private String suplementacaoSugerida;
    private String nomeRefeicaoSugerida = "Almoço Recomendado";
    private String alimentosSugeridos = "Nenhuma sugestão encontrada para esta refeição.";
    private String caloriasRefeicao ="0";

    public DietaResponseDTO(br.com.smartmeal.smartmeal.model.nosql.DietaRecomendada dietaDoBanco) {

        this.custoEstimado = dietaDoBanco.getCustoEstimado();
        this.suplementacaoSugerida = dietaDoBanco.getSuplementacaoSugerida();

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(dietaDoBanco.getPlanoAlimentar());

            if(rootNode.has("plano_alimentar")) {
                this.planoAlimentar = mapper.treeToValue(rootNode.get("plano_alimentar"), PlanoAlimentarDTO.class);
            }else {
             this.planoAlimentar = mapper.treeToValue(rootNode, PlanoAlimentarDTO.class);
            }
        } catch (Exception e) {
            System.out.println("--- [ERRO] Falha ao converter o JSON da IA: " + e.getMessage());
            this.planoAlimentar = new PlanoAlimentarDTO();
        }

        try {
            if (this.planoAlimentar != null && this.planoAlimentar.getCardapioSemanal() != null && !this.planoAlimentar.getCardapioSemanal().isEmpty()) {
                // Pega o primeiro dia do cardápio (ex: Segunda-feira)
                Map<String, Object> primeiroDia = this.planoAlimentar.getCardapioSemanal().get(0);

                if (primeiroDia.containsKey("refeicoes")) {
                    Map<String, Object> refeicoes = (Map<String, Object>) primeiroDia.get("refeicoes");

                    // Vamos buscar especificamente pelo "almoco" ou "almoço"
                    String chaveAlmoco = refeicoes.containsKey("almoco") ? "almoco" : (refeicoes.containsKey("almoço") ? "almoço" : null);

                    if (chaveAlmoco != null) {
                        Map<String, Object> almoco = (Map<String, Object>) refeicoes.get(chaveAlmoco);

                        // Extrai a lista de alimentos e transforma em uma String bonita separada por vírgula
                        if (almoco.containsKey("alimentos")) {
                            this.alimentosSugeridos = String.join(", ", (List<String>) almoco.get("alimentos"));
                        }

                        // Extrai as calorias da refeição
                        if (almoco.containsKey("calorias_estimadas")) {
                            this.caloriasRefeicao = String.valueOf(almoco.get("calorias_estimadas"));
                        } else if (almoco.containsKey("calorias")) {
                            this.caloriasRefeicao = String.valueOf(almoco.get("calorias"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("--- [DEBUG] Não foi possível extrair a refeição pontual: " + e.getMessage());
        }
    }

    public String getNomeRefeicaoSugerida() { return nomeRefeicaoSugerida; }
    public String getAlimentosSugeridos() { return alimentosSugeridos; }
    public String getCaloriasRefeicao() { return caloriasRefeicao; }
}
