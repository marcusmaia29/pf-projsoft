package insper.pf_projsoft.artigo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ArtigoService {
    private final ArtigoRepository repository;

    public ArtigoService(ArtigoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ArtigoResponse criar(ArtigoRequest request, String emailCriador) {
        if (emailCriador == null || emailCriador.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O token não informa o e-mail do usuário.");
        }
        var artigo = new Artigo(request.titulo().trim(), request.descricao().trim(),
                request.prioridade().trim(), emailCriador.trim());
        return ArtigoResponse.de(repository.save(artigo));
    }

    @Transactional(readOnly = true)
    public List<ArtigoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(ArtigoResponse::de).toList();
    }

    @Transactional
    public void deletar(Long id) {
        var artigo = repository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Artigo não encontrado."));
        repository.delete(artigo);
    }
}
