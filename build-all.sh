#!/bin/bash

echo "🏗️  Compilando todos los microservicios..."

# Transaction Service
echo "📦 Compilando ms-ne-transaction-service..."
cd ms-ne-transaction-service
gradle clean build -x test
cd ..

# Anti-Fraud Service
echo "🛡️  Compilando ms-sp-antifraud-rules..."
cd ms-sp-antifraud-rules
gradle clean build -x test
cd ..

# Orchestrator
echo "🎯 Compilando ms-ux-orchestrator..."
cd ms-ux-orchestrator
gradle clean build -x test
cd ..

echo "✅ Todos los servicios compilados exitosamente!"
echo "🐳 Ejecuta 'docker-compose up --build' para levantar los servicios"

