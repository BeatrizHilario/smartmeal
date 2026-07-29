package br.com.smartmeal.smartmeal.service;

import br.com.smartmeal.smartmeal.model.nosql.RegistroDiario;
import br.com.smartmeal.smartmeal.repository.nosql.RegistroDiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DiarioService {

    @Autowired
    private RegistroDiarioRepository repository;

    @Autowired
    private ArtificialIntelligenceService aiService;

    public void registrarRefeicaoSugerida(String usuarioId, String tipoRefeicao, String descricao, Integer calorias) {
        RegistroDiario registro = new RegistroDiario();
        registro.setUsuarioId(usuarioId);
        registro.setData(LocalDate.now());
        registro.setTipoRefeicao(tipoRefeicao);
        registro.setDescricao(descricao);
        registro.setCalorias(calorias);
        registro.setSugestaoIa(true);

        repository.save(registro);
    }

    public void registrarRefeicaoManual(String usuarioId, String tipoRefeicao, String descricao) {
        Integer caloriasEstimadas = aiService.estimarCaloriasRefeicaoLivre(descricao);

        RegistroDiario registro = new RegistroDiario();
        registro.setUsuarioId(usuarioId);
        registro.setData(LocalDate.now());
        registro.setTipoRefeicao(tipoRefeicao);
        registro.setDescricao(descricao);
        registro.setCalorias(caloriasEstimadas);
        registro.setSugestaoIa(false);

        repository.save(registro);
    }

    public List<RegistroDiario> buscarRefeicoesDoDia(String usuarioId, LocalDate data) {
        return repository.findByUsuarioIdAndData(usuarioId, data);
    }

    public boolean isRefeicaoRegistrada(String usuarioId, LocalDate data, String tipoRefeicao) {
        RegistroDiario refeicao = repository.findByUsuarioIdAndDataAndTipoRefeicao(usuarioId, data, tipoRefeicao);
        return refeicao != null;
    }

    public void excluirRefeicao(String registroId) {
        repository.deleteById(registroId);
    }
}
