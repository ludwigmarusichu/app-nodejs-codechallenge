package pe.com.yape.ms.transaction.model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que representa una transacción financiera
 * Usa Java 21 Record para inmutabilidad y expresividad
 * 
 * @author Yape Engineering Team
 * @version 1.0.0
 */
public record Transaction(
    UUID transactionExternalId,
    UUID accountExternalIdDebit,
    UUID accountExternalIdCredit,
    TransactionType transactionType,
    BigDecimal value,
    TransactionStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    /**
     * Constructor compacto con validaciones de negocio
     */
    public Transaction {
        if (transactionExternalId == null) {
            throw new IllegalArgumentException("Transaction external ID cannot be null");
        }
        if (accountExternalIdDebit == null) {
            throw new IllegalArgumentException("Account external ID debit cannot be null");
        }
        if (accountExternalIdCredit == null) {
            throw new IllegalArgumentException("Account external ID credit cannot be null");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction value must be greater than zero");
        }
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at cannot be null");
        }
    }
    
    /**
     * Factory method para crear una nueva transacción en estado PENDING
     */
    public static Transaction createPending(
            UUID accountExternalIdDebit,
            UUID accountExternalIdCredit,
            TransactionType transactionType,
            BigDecimal value
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(
            UUID.randomUUID(),
            accountExternalIdDebit,
            accountExternalIdCredit,
            transactionType,
            value,
            TransactionStatus.PENDING,
            now,
            now
        );
    }
    
    /**
     * Crea una nueva transacción con el estado actualizado
     * Patrón inmutable: retorna nueva instancia en lugar de modificar
     */
    public Transaction withStatus(TransactionStatus newStatus) {
        return new Transaction(
            this.transactionExternalId,
            this.accountExternalIdDebit,
            this.accountExternalIdCredit,
            this.transactionType,
            this.value,
            newStatus,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * Verifica si la transacción está en estado pendiente
     */
    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }
    
    /**
     * Verifica si la transacción fue aprobada
     */
    public boolean isApproved() {
        return this.status == TransactionStatus.APPROVED;
    }
    
    /**
     * Verifica si la transacción fue rechazada
     */
    public boolean isRejected() {
        return this.status == TransactionStatus.REJECTED;
    }
}

