package br.com.smartmeal.smartmeal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "tabela_nutricional")
public class TabelaNutricional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "calorias_100g", nullable = false)
    private BigDecimal calorias100g;

    @Column(name = "proteinas_100g", nullable = false)
    private BigDecimal proteinas100g;

    @Column(name = "gorduras_100g", nullable = false)
    private BigDecimal gorduras100g;

    @Column(name = "carboidratos_100g", nullable = false)
    private BigDecimal carboidratos100g;

}
