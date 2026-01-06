package pe.com.yape.orchestrator.application.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import pe.com.yape.orchestrator.model.response.ErrorResponse;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

/**
 * Manejador global de excepciones para el ms
 * 
 * @author lmarusic
 * @version 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(TransactionServiceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTransactionServiceException(
            TransactionServiceException ex,
            ServerWebExchange exchange
    ) {
        log.error("Transaction service error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .message("Error communicating with transaction service: " + ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(error));
    }
    
    @ExceptionHandler(TimeoutException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTimeoutException(
            TimeoutException ex,
            ServerWebExchange exchange
    ) {
        log.error("Timeout error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.GATEWAY_TIMEOUT.value())
                .message("Request timeout: " + ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(error));
    }
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex,
                                                                         ServerWebExchange exchange) {
        log.error("Validation error", ex);
        
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                 errors.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed: " + errors.toString())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error));
    }
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex,
                                                                      ServerWebExchange exchange) {
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred: " + ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error));
    }
}

