package insper.pf_projsoft.artigo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "artigos")
public class Artigo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String descricao;

    @Column(nullable = false, length = 50)
    private String prioridade;

    @Column(name = "email_criador", nullable = false, length = 320)
    private String emailCriador;

    protected Artigo() {
    }

    public Artigo(String titulo, String descricao, String prioridade, String emailCriador) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.emailCriador = emailCriador;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public String getEmailCriador() {
        return emailCriador;
    }
}
