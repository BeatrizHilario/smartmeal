package br.com.smartmeal.smartmeal.model.nosql;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "diario_alimentar")
public class RegistroDiario {

    @Id
    private String id;

    private String usuarioId;
    private LocalDate data;
    private String tipoRefeicao;
    private String descricao;
    private Integer calorias;
    private boolean sugestaoIa;

}
