package insper.pf_projsoft.artigo;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> dadosInvalidos(MethodArgumentNotValidException erro) {
        List<String> mensagens = erro.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getDefaultMessage()).sorted().toList();
        return resposta(HttpStatus.BAD_REQUEST, String.join(" ", mensagens));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> requisicaoInvalida(Exception erro) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> statusConhecido(ResponseStatusException erro) {
        var status = HttpStatus.valueOf(erro.getStatusCode().value());
        return resposta(status, erro.getReason() == null ? status.getReasonPhrase() : erro.getReason());
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(Map.of("status", status.value(), "erro", mensagem));
    }
}
