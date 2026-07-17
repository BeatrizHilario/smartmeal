package br.com.smartmeal.smartmeal.repository;

import br.com.smartmeal.smartmeal.model.RestricaoPaladar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestricaoPaladarRepository extends JpaRepository<RestricaoPaladar, Integer> {
    List<RestricaoPaladar> findByUsuarioIdUsuario(Integer idUsuario);
}
