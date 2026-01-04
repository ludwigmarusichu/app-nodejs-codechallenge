package pe.com.yape.ms.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * MS-NE-TRANSACTION-SERVICE
 * Servicio de negocio core para gestión de transacciones
 * 
 * @author Yape Engineering Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}

