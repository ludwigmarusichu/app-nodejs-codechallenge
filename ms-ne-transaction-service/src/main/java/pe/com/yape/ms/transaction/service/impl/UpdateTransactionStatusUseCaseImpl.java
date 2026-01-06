package pe.com.yape.ms.transaction.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.yape.ms.transaction.application.exception.InvalidTransactionStateException;
import pe.com.yape.ms.transaction.application.exception.TransactionNotFoundException;
import pe.com.yape.ms.transaction.model.domain.Transaction;
import pe.com.yape.ms.transaction.model.domain.TransactionStatus;
import pe.com.yape.ms.transaction.service.port.CacheRepositoryPort;
import pe.com.yape.ms.transaction.service.port.TransactionRepositoryPort;
import pe.com.yape.ms.transaction.service.UpdateTransactionStatusUseCase;

/**
 * Implementación del caso de uso: UpdateTransactionStatusUseCase
 *
 * @author lmarusic
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatusUseCaseImpl implements UpdateTransactionStatusUseCase {
    
    private final TransactionRepositoryPort transactionRepository;
    private final CacheRepositoryPort cacheRepository;
    
    private static final long CACHE_TTL_SECONDS = 3600L; // 1 hora
    
    @Override
    @Transactional
    public Transaction execute(UUID transactionExternalId, TransactionStatus newStatus) {
        log.info("Updating transaction {} to status: {}", transactionExternalId, newStatus);
        
        // 1. Buscar transacción
        Transaction currentTransaction = transactionRepository
                .findByExternalId(transactionExternalId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for update: {}", transactionExternalId);
                    return new TransactionNotFoundException(transactionExternalId);
                });
        
        // 2. Validar que esté en estado PENDING
        if (!currentTransaction.isPending()) {
            log.warn("Attempted to update non-pending transaction: {} from {} to {}",
                    transactionExternalId, currentTransaction.status(), newStatus);
            throw new InvalidTransactionStateException(
                    transactionExternalId,
                    currentTransaction.status(),
                    newStatus
            );
        }
        
        // 3. Crear nueva instancia con estado actualizado (inmutabilidad)
        Transaction updatedTransaction = currentTransaction.withStatus(newStatus);
        
        // 4. Guardar en BD
        // Debezium CDC capturará este UPDATE automáticamente desde WAL
        // y publicará el evento 'transaction.updated' a Kafka
        Transaction savedTransaction = transactionRepository.update(updatedTransaction);
        
        // 5. Actualizar cache
        cacheRepository.save(savedTransaction, CACHE_TTL_SECONDS);
        
        log.info("Transaction updated successfully: {} - Status: {}",
                savedTransaction.transactionExternalId(), savedTransaction.status());
        
        return savedTransaction;
    }
}

