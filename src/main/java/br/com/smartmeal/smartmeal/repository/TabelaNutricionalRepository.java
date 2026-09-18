package br.com.smartmeal.smartmeal.repository;

import br.com.smartmeal.smartmeal.model.TabelaNutricional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TabelaNutricionalRepository extends JpaRepository<TabelaNutricional, Integer> {

}
