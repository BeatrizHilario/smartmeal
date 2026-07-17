package br.com.smartmeal.smartmeal.model.nosql;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "dieta_recomendada")
public class DietaRecomendada {

    @Id
    private String id;

    @Field("idusuario")
    private Integer idUsuario;
    private LocalDateTime dataGeracao;
    private Double custoEstimado;
    private String planoAlimentar;
    private String suplementacaoSugerida;


}
