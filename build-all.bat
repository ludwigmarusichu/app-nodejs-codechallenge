@echo off
echo 🏗️  Compilando todos los microservicios...

echo 📦 Compilando ms-ne-transaction-service...
cd ms-ne-transaction-service
call gradle clean build -x test
cd ..

echo 🛡️  Compilando ms-sp-antifraud-rules...
cd ms-sp-antifraud-rules
call gradle clean build -x test
cd ..

echo 🎯 Compilando ms-ux-orchestrator...
cd ms-ux-orchestrator
call gradle clean build -x test
cd ..

echo ✅ Todos los servicios compilados exitosamente!
echo 🐳 Ejecuta 'docker-compose up --build' para levantar los servicios

