package br.com.smartmeal.smartmeal.controller;

import br.com.smartmeal.smartmeal.model.Usuario;
import br.com.smartmeal.smartmeal.model.nosql.RegistroDiario;
import br.com.smartmeal.smartmeal.service.DiarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Controller
@RequestMapping("/diario")
public class DiarioController {

    @Autowired
    private DiarioService diarioService;

    @GetMapping
    public String exibirDiario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        LocalDate hoje = LocalDate.now();
        List<RegistroDiario> refeicoesHoje = diarioService.buscarRefeicoesDoDia(usuario.getIdUsuario().toString(), hoje);

        model.addAttribute("refeicoes", refeicoesHoje);
        model.addAttribute("usuario", usuario);

        return "diario-alimentar";
    }

    @PostMapping("registrar-sugestao")
    public String registrarSugestao(@RequestParam String tipoRefeicao, @RequestParam String descricao, @RequestParam Integer calorias, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario != null) {
            diarioService.registrarRefeicaoSugerida(usuario.getIdUsuario().toString(), tipoRefeicao, descricao, calorias);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/registrar-manual")
    public String registrarManual(@RequestParam String tipoRefeicao, @RequestParam String descricao, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario != null) {
            diarioService.registrarRefeicaoManual(usuario.getIdUsuario().toString(), tipoRefeicao, descricao);
        }
        return "redirect:/diario";
    }
}
