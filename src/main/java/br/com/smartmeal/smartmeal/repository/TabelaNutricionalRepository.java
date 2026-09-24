package br.com.smartmeal.smartmeal.repository;

import br.com.smartmeal.smartmeal.model.TabelaNutricional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TabelaNutricionalRepository extends JpaRepository<TabelaNutricional, Integer> {
    @Query(value = "SELECT * FROM public.tabela_nutricional WHERE LOWER(nome) LIKE LOWER(CONCAT('%', :termo, '%')) ORDER BY LENGTH(nome) ASC LIMIT 1", nativeQuery = true)
    Optional<TabelaNutricional> buscarPorNomeAproximado(@Param("termo") String termo);
}
