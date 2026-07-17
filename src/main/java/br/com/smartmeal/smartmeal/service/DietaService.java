package br.com.smartmeal.smartmeal.service;

import br.com.smartmeal.smartmeal.model.Usuario;
import br.com.smartmeal.smartmeal.model.nosql.DietaRecomendada;
import br.com.smartmeal.smartmeal.repository.UsuarioRepository;
import br.com.smartmeal.smartmeal.repository.nosql.DietaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DietaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DietaRepository dietaRepository;

    @Autowired
    private ArtificialIntelligenceService aiService;

    public DietaRecomendada processarEGerarDieta(Integer idUsuario) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no sistema."));

        String jsonRespostaDaIA = aiService.gerarDietaPelaIA(usuario, "Almoço");

        DietaRecomendada novaDieta = new DietaRecomendada();
        novaDieta.setIdUsuario(usuario.getIdUsuario());
        novaDieta.setDataGeracao(LocalDateTime.now());

        novaDieta.setCustoEstimado(usuario.getOrcamentoMaxMensal().doubleValue() * 0.9);
        novaDieta.setPlanoAlimentar(jsonRespostaDaIA);
        novaDieta.setSuplementacaoSugerida("Análise de suplementação inclusa no plano alimentar.");

        return dietaRepository.save(novaDieta);
    }

    public DietaRecomendada buscarPorIdUsuario(Integer idUsuario) {

        java.util.List<DietaRecomendada> dietas = dietaRepository.findByIdUsuario(idUsuario);

        if(dietas != null && !dietas.isEmpty()) {
            return dietas.get(0);
        }

        return null;
    }

    public DietaRecomendada salvarDieta(DietaRecomendada dieta) {
        return dietaRepository.save(dieta);
    }

    public void excluirDietasDoUsuario(Integer idUsuario) {
        dietaRepository.deleteAllByIdUsuario(idUsuario);
    }
}
