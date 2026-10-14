# Cuidado Seguro - RabbitMQ Queues

## Included services

- **RabbitMQ**: AMQP `5672`, administration UI `15672`.
- **queues**: Spring Boot API on port `8086`.
- **PDF FastAPI**: not included in this ZIP; the queues service calls it at `PDF_SERVICE_URL` (default in Compose: `http://host.docker.internal:8000`).

The Spring Boot application declares its exchanges, queues and bindings in `RabbitConfig.java` when it connects to RabbitMQ. This Compose file does not replace those definitions.

## Start

Run these commands from the `Queue_v7` folder (the folder containing `docker-compose.yml`):

```powershell
Copy-Item .env.example .env
docker compose up -d --build
docker compose ps
docker compose logs -f rabbitmq queues
```

Wait for both services to show as running/healthy.

## URLs

- RabbitMQ Management: http://localhost:15672
- Queues API: http://localhost:8086/api

Use the credentials in `.env` to sign in to the RabbitMQ Management UI. If you edit `.env` after the first startup, existing RabbitMQ data may retain the original credentials because they are initialized when the data directory is first created.

## Verify the AMQP port on Windows

```powershell
Test-NetConnection localhost -Port 5672
netstat -ano | findstr :5672
```

Docker Desktop must be running for these commands and `docker compose` to work as expected.

## Test the API

Send a sample request to create a PDF through the Direct exchange (the FastAPI service must be running on port 8000):

```powershell
$body = @{
  rut = "12345678-9"
  nombre = "Paciente de prueba"
  fecha_nacimiento = "2000-01-01"
  tipo_documento = "Ficha clínica"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8086/api/pdf_direct" `
  -ContentType "application/json" `
  -Body $body
```

The response returns a `requestId`. Poll/download with:

```powershell
Invoke-WebRequest -Uri "http://localhost:8086/api/descargar/PEGAR_REQUEST_ID"
```

Replace `PEGAR_REQUEST_ID` with the ID returned by the request. If PDF FastAPI runs inside another Docker Compose project rather than directly on Windows, put both projects on a shared Docker network and set `PDF_SERVICE_URL` to the FastAPI service name, for example `http://pdf-service:8000`.

## Stop safely

```powershell
docker compose down
```

This stops and removes the containers but keeps the named `rabbitmq_data` volume. Do not use `docker compose down -v` unless you intentionally want to delete persisted RabbitMQ data.
