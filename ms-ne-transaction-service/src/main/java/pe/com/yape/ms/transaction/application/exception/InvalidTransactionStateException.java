package pe.com.yape.ms.transaction.application.exception;

import java.util.UUID;
import pe.com.yape.ms.transaction.model.domain.TransactionStatus;

/**
 * Excepcion lanzada cuando se intenta actualizar una transaccion que no deberia ser actualizada
 * 
 * @author lmarusic
 * @version 1.0.0
 */
public class InvalidTransactionStateException extends RuntimeException {
    
    private final UUID transactionExternalId;
    private final TransactionStatus currentStatus;
    private final TransactionStatus attemptedStatus;
    
    public InvalidTransactionStateException(
            UUID transactionExternalId,
            TransactionStatus currentStatus,
            TransactionStatus attemptedStatus
    ) {
        super(String.format(
            "Cannot update transaction %s from status %s to %s. Only PENDING transactions can be updated.",
            transactionExternalId, currentStatus, attemptedStatus
        ));
        this.transactionExternalId = transactionExternalId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
    
    public UUID getTransactionExternalId() {
        return transactionExternalId;
    }
    
    public TransactionStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public TransactionStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}

