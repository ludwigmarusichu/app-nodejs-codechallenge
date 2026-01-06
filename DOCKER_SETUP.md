# Guía de Ejecución con Docker Compose

## Ejecución Completa con Un Solo Comando

```bash
docker-compose up -d --build
```

Este comando ejecutará automáticamente:

1. ✅ **Compilación de proyectos Gradle**
   - Transaction Service (con Flyway migrations)
   - Orchestrator Service
   - Antifraud Service (con generación de clases Avro)

2. ✅ **Generación de clases Avro**
   - `TransactionCreatedEvent.java`
   - `TransactionValidatedEvent.java`
   - `ValidationStatus.java`

3. ✅ **Registro de esquemas en Schema Registry**
   - `yape.public.transactions-value`
   - `transaction.validated-value`

4. ✅ **Levantamiento de toda la infraestructura**
   - PostgreSQL con CDC habilitado
   - Redis
   - Kafka + Zookeeper
   - Schema Registry
   - Kafka Connect con Debezium
   - Control Center
   - 3 Microservicios

## Orden de Inicio

```
1. postgres, redis, zookeeper
2. kafka
3. schema-registry
4. schema-registry-init (registra esquemas)
5. transaction-service (ejecuta migraciones)
6. orchestrator-service
7. connect (Debezium CDC)
8. antifraud-service
9. control-center
```

## Verificar que Todo Funciona

### 1. Ver logs de compilación
```bash
docker-compose logs -f antifraud-service
```

### 2. Verificar esquemas registrados
```bash
curl http://localhost:8081/subjects
```

Deberías ver:
```json
[
  "yape.public.transactions-value",
  "transaction.validated-value"
]
```

### 3. Ver esquema específico
```bash
curl http://localhost:8081/subjects/transaction.validated-value/versions/latest
```

### 4. Verificar servicios activos
```bash
docker-compose ps
```

### 5. Health checks
```bash
curl http://localhost:9992/actuator/health
curl http://localhost:9991/actuator/health
curl http://localhost:9993/actuator/health
```

## Puertos Expuestos

| Servicio | Puerto | URL |
|----------|--------|-----|
| PostgreSQL | 5432 | jdbc:postgresql://localhost:5432/yape_db |
| Redis | 6379 | localhost:6379 |
| Kafka | 9092 | localhost:9092 |
| Schema Registry | 8081 | http://localhost:8081 |
| Kafka Connect | 8083 | http://localhost:8083 |
| Control Center | 9021 | http://localhost:9021 |
| Transaction Service | 9992 | http://localhost:9992 |
| Orchestrator | 9991 | http://localhost:9991 |
| Antifraud | 9993 | http://localhost:9993 |

## Comandos Útiles

### Reconstruir todo desde cero
```bash
docker-compose down -v
docker-compose up -d --build
```

### Ver logs en tiempo real
```bash
docker-compose logs -f

docker-compose logs -f antifraud-service
docker-compose logs -f transaction-service
docker-compose logs -f connect
```

### Reconstruir solo un servicio
```bash
docker-compose up -d --build antifraud-service
```

### Detener todo
```bash
docker-compose down
```

### Detener y eliminar volúmenes (datos)
```bash
docker-compose down -v
```

## Probar el Flujo Completo

### 1. Crear una transacción
```bash
curl -X POST http://localhost:9991/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountExternalIdDebit": "acc-001",
    "accountExternalIdCredit": "acc-002",
    "tranferTypeId": 1,
    "value": 500.0
  }'
```

### 2. Consultar estado
```bash
curl http://localhost:9991/api/transactions/{transactionId}
```

### 3. Ver eventos en Kafka
```bash
docker exec -it yape-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic yape.public.transactions \
  --from-beginning

docker exec -it yape-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic transaction.validated \
  --from-beginning
```

## Troubleshooting

### Problema: Antifraud no compila
```bash
docker-compose logs antifraud-service
```

### Problema: Esquemas no se registran
```bash
docker-compose logs schema-registry-init
docker-compose restart schema-registry-init
```

### Problema: Debezium no captura eventos
```bash
curl http://localhost:8083/connectors/yape-transaction-connector/status
```

### Problema: Servicios no se comunican
```bash
docker network inspect yape-network
```

## Desarrollo Local vs Docker

### Desarrollo Local
```bash
cd ms-sp-antifraud-rules
./gradlew generateAvroJava
./gradlew bootRun
```

### Con Docker
```bash
docker-compose up -d --build antifraud-service
```

## Limpieza Total

```bash
docker-compose down -v
docker system prune -a --volumes
```

## Notas Importantes

- ⚠️ La primera compilación tardará más (descarga dependencias)
- ⚠️ El servicio antifraud espera a que los esquemas estén registrados
- ⚠️ Transaction Service ejecuta Flyway migrations automáticamente
- ⚠️ Debezium registra el conector automáticamente al iniciar

## Arquitectura Multi-Stage Build

```
┌─────────────────────────────────────┐
│  Stage 1: Builder (JDK)             │
│  - Copia código fuente              │
│  - Ejecuta ./gradlew build          │
│  - Genera clases Avro               │
│  - Crea JAR ejecutable              │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Stage 2: Runtime (JRE)             │
│  - Copia solo el JAR                │
│  - Imagen ligera                    │
│  - Lista para ejecutar              │
└─────────────────────────────────────┘
```

**Ventaja:** No necesitas tener Java, Gradle ni Avro instalados localmente. Todo se compila dentro de Docker.

