package insper.pf_projsoft.artigo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtigoRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 4000, message = "A descrição deve ter no máximo 4000 caracteres.")
        String descricao,

        @NotBlank(message = "A prioridade é obrigatória.")
        @Size(max = 50, message = "A prioridade deve ter no máximo 50 caracteres.")
        String prioridade) {
}
