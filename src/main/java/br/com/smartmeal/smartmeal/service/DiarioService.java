package br.com.smartmeal.smartmeal.service;

import br.com.smartmeal.smartmeal.model.TabelaNutricional;
import br.com.smartmeal.smartmeal.model.nosql.RegistroDiario;
import br.com.smartmeal.smartmeal.repository.TabelaNutricionalRepository;
import br.com.smartmeal.smartmeal.repository.nosql.RegistroDiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiarioService {

    @Autowired
    private RegistroDiarioRepository repository;

    @Autowired
    private ArtificialIntelligenceService aiService;

    @Autowired
    private TabelaNutricionalRepository tabelaNutricionalRepository;

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
        int caloriasCalculadas = calcularCaloriasHibrido(descricao);

        RegistroDiario registro = new RegistroDiario();
        registro.setUsuarioId(usuarioId);
        registro.setData(LocalDate.now());
        registro.setTipoRefeicao(tipoRefeicao);
        registro.setDescricao(descricao);
        registro.setCalorias(caloriasCalculadas);
        registro.setSugestaoIa(false);

        repository.save(registro);
    }

    private int calcularCaloriasHibrido(String descricaoCompleta) {
        if (descricaoCompleta == null || descricaoCompleta.isBlank()) {
            return 0;
        }

        // Divide os itens caso venha no padrão: "150 g de Frango, 2 unidade(s) de Ovo"
        String[] itens = descricaoCompleta.split(",");
        double totalCalorias = 0.0;
        List<String> itensNaoEncontrados = new ArrayList<>();

        // Padrão regex para capturar: [quantidade] [unidade] de [alimento]
        Pattern pattern = Pattern.compile("^\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(g|ml|colher\\(es\\) de sopa|unidade\\(s\\)|fatia\\(s\\)|concha\\(s\\))\\s*(?:de\\s+)?(.+)$", Pattern.CASE_INSENSITIVE);

        for (String itemStr : itens) {
            String itemLimpo = itemStr.trim();
            Matcher matcher = pattern.matcher(itemLimpo);

            if (matcher.matches()) {
                double quantidade = Double.parseDouble(matcher.group(1).replace(",", "."));
                String unidade = matcher.group(2).toLowerCase();
                String nomeAlimento = matcher.group(3).trim();

                // Converte a unidade caseira para gramas estimadas
                double pesoEmGramas = converterParaGramas(quantidade, unidade);

                // Busca na tabela nutricional (TACO)
                Optional<TabelaNutricional> alimentoOpt = tabelaNutricionalRepository.buscarPorNomeAproximado(nomeAlimento);

                if (alimentoOpt.isPresent() && alimentoOpt.get().getCalorias100g() != null) {
                    double cal100g = alimentoOpt.get().getCalorias100g().doubleValue();
                    double calItem = (pesoEmGramas * cal100g) / 100.0;
                    totalCalorias += calItem;
                    System.out.println("[TACO LOCAL] Encontrado: " + alimentoOpt.get().getNome() + " | " + calItem + " kcal");
                } else {
                    // Guarda o item para ser calculado pela IA
                    itensNaoEncontrados.add(itemLimpo);
                }
            } else {
                // Se o formato não bateu com a expressão regular, envia para a IA
                itensNaoEncontrados.add(itemLimpo);
            }
        }

        // Se algum item não constar na TACO, delega apenas as pendências à IA
        if (!itensNaoEncontrados.isEmpty()) {
            String pendencias = String.join(", ", itensNaoEncontrados);
            System.out.println("[FALLBACK IA] Calculando itens ausentes via IA: " + pendencias);
            try {
                Integer caloriasIa = aiService.estimarCaloriasRefeicaoLivre(pendencias);
                if (caloriasIa != null) {
                    totalCalorias += caloriasIa;
                }
            } catch (Exception e) {
                System.err.println("Erro no fallback da IA: " + e.getMessage());
            }
        }

        return (int) Math.round(totalCalorias);
    }

    private double converterParaGramas(double quantidade, String unidade) {
        return switch (unidade) {
            case "g", "ml" -> quantidade;
            case "colher(es) de sopa" -> quantidade * 15.0; // 1 colher de sopa ~ 15g
            case "unidade(s)" -> quantidade * 50.0;          // 1 unidade média padrão ~ 50g (ovo, fruta pequena)
            case "fatia(s)" -> quantidade * 30.0;            // 1 fatia padrão (pão, queijo) ~ 30g
            case "concha(s)" -> quantidade * 130.0;          // 1 concha média (feijão, sopa) ~ 130g
            default -> quantidade;
        };
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