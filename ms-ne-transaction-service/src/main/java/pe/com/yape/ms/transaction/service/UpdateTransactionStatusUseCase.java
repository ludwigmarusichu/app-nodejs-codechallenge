package pe.com.yape.ms.transaction.service;

import java.util.UUID;
import pe.com.yape.ms.transaction.model.domain.Transaction;
import pe.com.yape.ms.transaction.model.domain.TransactionStatus;

/**
 * Caso de Uso: Actualizar el estado de una transacción
 *
 * @author lamrusic
 * @version 1.0.0
 */
public interface UpdateTransactionStatusUseCase {
    
    /**
     * Actualiza el estado de una transacción existente
     *
     * @param transactionExternalId ID de la transacción
     * @param newStatus nuevo estado (APPROVED o REJECTED)
     * @return Transaction
     */
    Transaction execute(UUID transactionExternalId, TransactionStatus newStatus);
}

