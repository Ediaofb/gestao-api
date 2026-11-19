package br.com.lydero.gestao_api.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Transforma erros em JSON simples: {"erro": "mensagem"}. Sem isso, o Spring devolve
 * a resposta vazia e fica dificil entender o que deu errado (no Insomnia, por exemplo).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException ex) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", ex.getReason() != null ? ex.getReason() : "Erro na requisicao");
        return ResponseEntity.status(ex.getStatusCode()).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", "Dados invalidos");
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> corpoIlegivel(HttpMessageNotReadableException ex) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", "Corpo da requisicao ausente ou com JSON invalido");
        return ResponseEntity.badRequest().body(corpo);
    }
}
