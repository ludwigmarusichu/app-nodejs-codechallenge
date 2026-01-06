package pe.com.yape.ms.transaction.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de response para transacciones
 *
 * @author Yape Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private UUID transactionExternalId;
    
    private TransactionTypeDto transactionType;
    
    private TransactionStatusDto transactionStatus;
    
    private BigDecimal value;
    
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionTypeDto {
        private String name;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionStatusDto {
        private String name;
    }
}

