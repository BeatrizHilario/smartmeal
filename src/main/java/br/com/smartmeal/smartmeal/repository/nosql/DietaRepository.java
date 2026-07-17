package br.com.smartmeal.smartmeal.repository.nosql;

import br.com.smartmeal.smartmeal.model.nosql.DietaRecomendada;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietaRepository extends MongoRepository<DietaRecomendada, String> {

    List<DietaRecomendada> findByIdUsuario(Integer idUsuario);

    void deleteAllByIdUsuario(Integer idUsuario);
}
