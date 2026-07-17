package br.com.smartmeal.smartmeal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "altura_cm")
    private Integer alturaCm;

    @Column(nullable = false)
    private Integer idade;

    @Column(length = 50)
    private String objetivo;

    @Column(name = "orcamento_max_mensal", precision = 8, scale = 2)
    private BigDecimal orcamentoMaxMensal;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(name = "restricao_alimentar", length = 50)
    private String restricaoAlimentar;

    @Column(name = "genero")
    private String genero;

    @Column(name = "meta_agua_ml")
    private Integer metaAguaMl;

    @Column(name = "meta_calorica_kcal")
    private Integer metaCaloricaKcal;

    @Column(name = "nivel_atividade")
    private String nivelAtividade;
}
