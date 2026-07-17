package br.com.smartmeal.smartmeal.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanoAlimentarDTO {

    @JsonAlias({"nome_paciente", "NomePaciente"})
    private String nomePaciente;

    @JsonAlias({"idade", "Idade"})
    private String idade;

    @JsonProperty("Peso_Atual_Kg")
    private Double pesoAtualKg;

    @JsonProperty("Altura_Cm")
    private Integer alturaCm;

    @JsonAlias({"objetivo", "Objetivo", "objetivo_principal"})
    private String objetivo;

    @JsonProperty("TMB_Estimada_kcal")
    private Integer tmbEstimadaKcal;

    @JsonAlias({"necessidade_calorica_diaria_estimada_kcal", "Necessidade_Calorica_Diaria_Estimada_kcal", "meta_calorica", "calorias_diarias"})
    private Integer necessidadeCaloricaDiaria;

    @JsonAlias({"custo_total_estimado_mensal_r$", "Custo_Total_Estimado_Mensal_R$", "custo_estimado_mensal", "orcamento_mensal"})
    private String custoTotalEstimadoMensal;

    @JsonAlias({"regiao_referencia_precos", "Regiao_Referencia_Precos"})
    private String regiaoReferencia;

    @JsonAlias({"orientacoes_gerais", "Orientacoes_Gerais", "informacoes_adicionais"})
    private List<Object> orientacoesGerais;

    @JsonAlias({"exemplo_dia_alimentar_dia_1", "Exemplo_Dia_Alimentar_Dia_1"})
    private Map<String, Map<String, String>> refeicoes;

    @JsonAlias({"cardapio_semanal", "Cardapio_Semanal", "cardapio"})
    private List<Map<String, Object>> cardapioSemanal;

    public List<Map<String, Object>> getCardapioSemanal() {
        return cardapioSemanal;
    }

    public void setCardapioSemanal(List<Map<String, Object>> cardapioSemanal) {
        this.cardapioSemanal = cardapioSemanal;
    }

}
