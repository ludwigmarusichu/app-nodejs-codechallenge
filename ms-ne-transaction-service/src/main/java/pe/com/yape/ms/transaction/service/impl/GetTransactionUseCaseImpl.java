package pe.com.yape.ms.transaction.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.yape.ms.transaction.application.exception.TransactionNotFoundException;
import pe.com.yape.ms.transaction.model.domain.Transaction;
import pe.com.yape.ms.transaction.service.port.CacheRepositoryPort;
import pe.com.yape.ms.transaction.service.port.TransactionRepositoryPort;
import pe.com.yape.ms.transaction.service.GetTransactionUseCase;

/**
 * Implementación del caso de uso: GetTransactionUseCase
 *
 * @author lmarusic
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTransactionUseCaseImpl implements GetTransactionUseCase {
    
    private final TransactionRepositoryPort transactionRepository;
    private final CacheRepositoryPort cacheRepository;
    
    private static final long CACHE_TTL_SECONDS = 3600L; // 1 hora
    
    @Override
    @Transactional(readOnly = true)
    public Transaction execute(UUID transactionExternalId) {
        log.debug("Getting transaction: {}", transactionExternalId);
        
        // 1. Buscar en cache (Redis) - Fast path
        return cacheRepository.findByExternalId(transactionExternalId)
                .map(transaction -> {
                    log.info("Transaction found in cache: {}", transactionExternalId);
                    return transaction;
                })
                // 2. Si no está en cache, buscar en BD - Slow path
                .orElseGet(() -> {
                    log.debug("Transaction not in cache, searching in database: {}", transactionExternalId);
                    
                    Transaction transaction = transactionRepository
                            .findByExternalId(transactionExternalId)
                            .orElseThrow(() -> {
                                log.warn("Transaction not found: {}", transactionExternalId);
                                return new TransactionNotFoundException(transactionExternalId);
                            });
                    
                    // 3. Guardar en cache para futuras consultas
                    cacheRepository.save(transaction, CACHE_TTL_SECONDS);
                    log.info("Transaction found in database and cached: {}", transactionExternalId);
                    
                    return transaction;
                });
    }
}

