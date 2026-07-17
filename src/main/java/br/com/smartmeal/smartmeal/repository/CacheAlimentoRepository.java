package br.com.smartmeal.smartmeal.repository;

import br.com.smartmeal.smartmeal.model.CacheAlimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheAlimentoRepository extends JpaRepository<CacheAlimento, Integer> {
    boolean existsByNomeIgnoreCase(String nome);
}
