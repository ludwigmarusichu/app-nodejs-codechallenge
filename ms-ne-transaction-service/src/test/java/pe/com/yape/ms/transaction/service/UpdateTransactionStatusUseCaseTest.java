package pe.com.yape.ms.transaction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.yape.ms.transaction.application.exception.InvalidTransactionStateException;
import pe.com.yape.ms.transaction.application.exception.TransactionNotFoundException;
import pe.com.yape.ms.transaction.model.domain.Transaction;
import pe.com.yape.ms.transaction.model.domain.TransactionStatus;
import pe.com.yape.ms.transaction.model.domain.TransactionType;
import pe.com.yape.ms.transaction.service.port.CacheRepositoryPort;
import pe.com.yape.ms.transaction.service.port.TransactionRepositoryPort;
import pe.com.yape.ms.transaction.service.impl.UpdateTransactionStatusUseCaseImpl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UpdateTransactionStatusUseCase (TDD - GREEN Phase)
 * Verifica las reglas de negocio de actualización de estado
 * 
 * @author Yape Engineering Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Update Transaction Status Use Case Tests - GREEN Phase")
class UpdateTransactionStatusUseCaseTest {
    
    @Mock
    private TransactionRepositoryPort transactionRepository;
    
    @Mock
    private CacheRepositoryPort cacheRepository;
    
    private UpdateTransactionStatusUseCase updateTransactionStatusUseCase;
    
    @BeforeEach
    void setUp() {
        // Ahora SÍ existe la implementación
        updateTransactionStatusUseCase = new UpdateTransactionStatusUseCaseImpl(
            transactionRepository,
            cacheRepository
        );
    }
    
    @Test
    @DisplayName("RED: Debe actualizar transacción de PENDING a APPROVED")
    void shouldUpdateTransactionFromPendingToApproved() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction pendingTransaction = Transaction.createPending(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TransactionType.TRANSFER,
            new BigDecimal("500.00")
        );
        
        Transaction approvedTransaction = pendingTransaction.withStatus(TransactionStatus.APPROVED);
        
        when(transactionRepository.findByExternalId(transactionId))
            .thenReturn(Optional.of(pendingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenReturn(approvedTransaction);
        
        // When
        Transaction result = updateTransactionStatusUseCase.execute(
            transactionId,
            TransactionStatus.APPROVED
        );
        
        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.APPROVED, result.status());
        assertTrue(result.isApproved());
        verify(transactionRepository, times(1)).update(any(Transaction.class));
        verify(cacheRepository, times(1)).save(eq(result), anyLong());
    }
    
    @Test
    @DisplayName("RED: Debe actualizar transacción de PENDING a REJECTED")
    void shouldUpdateTransactionFromPendingToRejected() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction pendingTransaction = Transaction.createPending(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TransactionType.TRANSFER,
            new BigDecimal("1500.00")
        );
        
        Transaction rejectedTransaction = pendingTransaction.withStatus(TransactionStatus.REJECTED);
        
        when(transactionRepository.findByExternalId(transactionId))
            .thenReturn(Optional.of(pendingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenReturn(rejectedTransaction);
        
        // When
        Transaction result = updateTransactionStatusUseCase.execute(
            transactionId,
            TransactionStatus.REJECTED
        );
        
        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.status());
        assertTrue(result.isRejected());
        verify(transactionRepository, times(1)).update(any(Transaction.class));
    }
    
    @Test
    @DisplayName("GREEN: Debe lanzar excepción si la transacción no existe")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        UUID transactionId = UUID.randomUUID();
        
        when(transactionRepository.findByExternalId(transactionId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(TransactionNotFoundException.class, () ->
            updateTransactionStatusUseCase.execute(
                transactionId,
                TransactionStatus.APPROVED
            )
        );
        
        verify(transactionRepository, never()).update(any());
        verify(cacheRepository, never()).save(any(), anyLong());
    }
    
    @Test
    @DisplayName("GREEN: Debe lanzar excepción si la transacción no está en estado PENDING")
    void shouldThrowExceptionWhenTransactionIsNotPending() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction approvedTransaction = Transaction.createPending(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TransactionType.TRANSFER,
            new BigDecimal("100.00")
        ).withStatus(TransactionStatus.APPROVED);
        
        when(transactionRepository.findByExternalId(transactionId))
            .thenReturn(Optional.of(approvedTransaction));
        
        // When & Then
        assertThrows(InvalidTransactionStateException.class, () ->
            updateTransactionStatusUseCase.execute(
                transactionId,
                TransactionStatus.REJECTED
            )
        );
        
        verify(transactionRepository, never()).update(any());
    }
    
    @Test
    @DisplayName("RED: Debe actualizar el cache después de actualizar en DB")
    void shouldUpdateCacheAfterDatabaseUpdate() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction pendingTransaction = Transaction.createPending(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TransactionType.TRANSFER,
            new BigDecimal("300.00")
        );
        
        Transaction approvedTransaction = pendingTransaction.withStatus(TransactionStatus.APPROVED);
        
        when(transactionRepository.findByExternalId(transactionId))
            .thenReturn(Optional.of(pendingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenReturn(approvedTransaction);
        
        // When
        updateTransactionStatusUseCase.execute(transactionId, TransactionStatus.APPROVED);
        
        // Then - Verificar orden: DB update -> cache save
        var inOrder = inOrder(transactionRepository, cacheRepository);
        inOrder.verify(transactionRepository).update(any(Transaction.class));
        inOrder.verify(cacheRepository).save(eq(approvedTransaction), anyLong());
    }
}

