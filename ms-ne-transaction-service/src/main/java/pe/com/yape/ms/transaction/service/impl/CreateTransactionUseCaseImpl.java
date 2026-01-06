package pe.com.yape.ms.transaction.service.impl;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.yape.ms.transaction.model.domain.Transaction;
import pe.com.yape.ms.transaction.model.domain.TransactionType;
import pe.com.yape.ms.transaction.service.port.TransactionRepositoryPort;
import pe.com.yape.ms.transaction.service.CreateTransactionUseCase;

/**
 * Implementación del caso de uso: CreateTransactionUseCase
 * 
 * @author lmarusic
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTransactionUseCaseImpl implements CreateTransactionUseCase {
    
    private final TransactionRepositoryPort transactionRepository;
    
    @Override
    @Transactional
    public Transaction execute(
            UUID accountExternalIdDebit,
            UUID accountExternalIdCredit,
            int transferTypeId,
            BigDecimal value
    ) {
        log.info("Creating transaction - Debit: {}, Credit: {}, Type: {}, Value: {}",
                accountExternalIdDebit, accountExternalIdCredit, transferTypeId, value);
        
        // Validar datos de entrada
        validateInputs(accountExternalIdDebit, accountExternalIdCredit, value);
        
        // Obtener el tipo de transacción
        TransactionType transactionType = TransactionType.fromId(transferTypeId);
        
        // Crear transacción en estado PENDING usando factory method
        Transaction transaction = Transaction.createPending(
                accountExternalIdDebit,
                accountExternalIdCredit,
                transactionType,
                value
        );
        
        // Guardar en PostgreSQL
        // Debezium CDC capturará este INSERT automáticamente desde WAL
        // y publicará el evento 'transaction.created' a Kafka
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Transaction created successfully: {} with status: {}",
                savedTransaction.transactionExternalId(), savedTransaction.status());
        
        return savedTransaction;
    }
    
    private void validateInputs(UUID accountDebit, UUID accountCredit, BigDecimal value) {
        if (accountDebit == null) {
            throw new IllegalArgumentException("Account external ID debit cannot be null");
        }
        if (accountCredit == null) {
            throw new IllegalArgumentException("Account external ID credit cannot be null");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction value must be greater than zero");
        }
    }
}

