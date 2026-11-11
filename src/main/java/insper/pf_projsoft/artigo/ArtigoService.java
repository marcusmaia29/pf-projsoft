package insper.pf_projsoft.artigo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ArtigoService {
    private final ArtigoRepository repository;

    public ArtigoService(ArtigoRepository repository) {
        this.repository = repository;
    }

    public Artigo criar(Artigo artigo, String emailCriador) {
        artigo.setId(null);
        artigo.setEmailCriador(emailCriador);
        return repository.save(artigo);
    }

    public List<Artigo> listar() {
        return repository.findAll();
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artigo não encontrado.");
        }
        repository.deleteById(id);
    }
}
