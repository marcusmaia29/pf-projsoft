package insper.pf_projsoft.artigo;

public record ArtigoResponse(Long id, String titulo, String descricao, String prioridade,
                             String emailCriador) {
    public static ArtigoResponse de(Artigo artigo) {
        return new ArtigoResponse(artigo.getId(), artigo.getTitulo(), artigo.getDescricao(),
                artigo.getPrioridade(), artigo.getEmailCriador());
    }
}
