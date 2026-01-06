package pe.com.yape.ms.transaction.controller.mapper;

import org.springframework.stereotype.Component;
import pe.com.yape.ms.transaction.controller.dto.TransactionResponse;
import pe.com.yape.ms.transaction.model.domain.Transaction;

/**
 * Mapper para convertir entre objetos de dominio y DTOs
 * 
 * @author lmarusic
 * @version 1.0.0
 */
@Component
public class TransactionMapper {
    
    /**
     * Convierte una transacción del dominio a DTO de respuesta
     */
    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionExternalId(transaction.transactionExternalId())
                .transactionType(TransactionResponse.TransactionTypeDto.builder()
                        .name(transaction.transactionType().getName())
                        .build())
                .transactionStatus(TransactionResponse.TransactionStatusDto.builder()
                        .name(transaction.status().getValue())
                        .build())
                .value(transaction.value())
                .createdAt(transaction.createdAt())
                .build();
    }
}

