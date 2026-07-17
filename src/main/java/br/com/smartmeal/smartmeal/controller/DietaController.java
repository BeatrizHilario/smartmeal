package br.com.smartmeal.smartmeal.controller;

import br.com.smartmeal.smartmeal.model.dto.DietaResponseDTO;
import br.com.smartmeal.smartmeal.model.nosql.DietaRecomendada;
import br.com.smartmeal.smartmeal.service.DietaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dietas")
public class DietaController {

    @Autowired
    private DietaService dietaService;

    @PostMapping("/gerar/{idUsuario}")
    public ResponseEntity<DietaResponseDTO> gerarDietaReal(@PathVariable Integer idUsuario) {
        DietaRecomendada dietaGerada = dietaService.processarEGerarDieta(idUsuario);

        DietaResponseDTO response = new DietaResponseDTO(dietaGerada);

        return ResponseEntity.ok(response);
    }
}
