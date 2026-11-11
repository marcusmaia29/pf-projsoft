package insper.pf_projsoft.artigo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "artigos")
public class Artigo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank
    @Size(max = 4000)
    @Column(nullable = false, length = 4000)
    private String descricao;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String prioridade;

    @Column(name = "email_criador", nullable = false, length = 320)
    private String emailCriador;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getEmailCriador() {
        return emailCriador;
    }

    public void setEmailCriador(String emailCriador) {
        this.emailCriador = emailCriador;
    }
}
