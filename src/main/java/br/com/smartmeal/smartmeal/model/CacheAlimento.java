package br.com.smartmeal.smartmeal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cache_alimentos")
public class CacheAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAlimento;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "calorias_por_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal caloriasPor100g;

    @Column(name = "proteinas_g", nullable = false, precision = 5, scale = 2)
    private BigDecimal proteinasG;

    @Column(name = "carboidratos_g", nullable = false, precision = 5, scale = 2)
    private BigDecimal carboidratosG;

    @Column(name = "gorduras_g", nullable = false, precision = 5, scale = 2)
    private BigDecimal gordurasG;

    @Column(name = "preco_medio_base", nullable = false, precision = 8, scale = 2)
    private BigDecimal precoMedioBase;

    @Column(name = "data_ultima_atualizacao", nullable = false)
    private LocalDate dataUltimaAtualizacao;

    @Column(name = "fonte_dados", length = 50)
    private String fonteDados;
}
