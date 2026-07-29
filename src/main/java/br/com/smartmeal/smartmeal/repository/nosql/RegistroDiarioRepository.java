package br.com.smartmeal.smartmeal.repository.nosql;

import br.com.smartmeal.smartmeal.model.nosql.RegistroDiario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface RegistroDiarioRepository extends MongoRepository<RegistroDiario, String> {
    List<RegistroDiario> findByUsuarioIdAndData(String usuarioId, LocalDate data);

    RegistroDiario findByUsuarioIdAndDataAndTipoRefeicao(String usuarioId, LocalDate data, String tipoRefeicao);
}
