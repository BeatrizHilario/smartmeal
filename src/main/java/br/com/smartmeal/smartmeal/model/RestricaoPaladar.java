package br.com.smartmeal.smartmeal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restricao_paladar")
public class RestricaoPaladar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRestricao;

    @Column(name = "tipo_restricao", nullable = false, length = 50)
    private String tipoRestricao;

    @Column(nullable = false, length = 100)
    private String ingrediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}
