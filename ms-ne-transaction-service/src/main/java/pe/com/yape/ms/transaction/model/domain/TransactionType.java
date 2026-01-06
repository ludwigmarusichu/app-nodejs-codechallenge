package pe.com.yape.ms.transaction.model.domain;

/**
 * Tipo de transacción financiera
 * 
 * @author Yape Engineering Team
 * @version 1.0.0
 */
public enum TransactionType {
    /**
     * Transferencia entre cuentas
     */
    TRANSFER(1, "Transfer"),
    
    /**
     * Pago de servicios
     */
    PAYMENT(2, "Payment"),
    
    /**
     * Retiro de efectivo
     */
    WITHDRAWAL(3, "Withdrawal"),
    
    /**
     * Depósito
     */
    DEPOSIT(4, "Deposit");
    
    private final int id;
    private final String name;
    
    TransactionType(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Obtiene el tipo de transacción por ID
     * 
     * @param id identificador del tipo
     * @return enum correspondiente
     * @throws IllegalArgumentException si el ID no es válido
     */
    public static TransactionType fromId(int id) {
        for (TransactionType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type id: " + id);
    }
}

