# Cuidado Seguro - RabbitMQ

## 1. Descripción del proyecto

Este repositorio contiene la configuración de la infraestructura de **RabbitMQ** para el proyecto Cuidado Seguro.

RabbitMQ se utilizará como un **Message Broker independiente**, encargado de recibir, enrutar y distribuir mensajes entre los microservicios mediante colas, permitiendo implementar comunicación asíncrona dentro de la arquitectura.

Se ha seleccionado **Topic Exchange** como estrategia de enrutamiento, ya que permite distribuir mensajes utilizando patrones de Routing Keys según los eventos que deban procesar los distintos consumidores.

## 2. Objetivos

- Implementar RabbitMQ como infraestructura independiente de los microservicios.
- Facilitar la comunicación asíncrona entre los componentes de Cuidado Seguro.
- Configurar un Topic Exchange para distribuir mensajes mediante patrones.
- Permitir que distintos consumidores procesen los eventos correspondientes.
- Incorporar progresivamente mecanismos de confiabilidad y gestión de mensajes.

## 3. Tecnologías utilizadas

- **RabbitMQ:** Message Broker.
- **Docker:** ejecución de RabbitMQ en un contenedor.
- **Docker Compose:** configuración y administración del contenedor.
- **RabbitMQ Management:** interfaz web para administrar y monitorear RabbitMQ.
- **Git y GitHub:** control de versiones.

## 4. Arquitectura

RabbitMQ funcionará como un componente independiente al que podrán conectarse los microservicios productores y consumidores.

```text
Cuidado Seguro
      |
      +-- Auth Service
      |
      +-- Pacientes Service
      |
      +-- Datos Médicos Service
                  |
                  | Publicación de mensajes
                  v
             RabbitMQ
                  |
                  v
            Topic Exchange
                  |
             Bindings
                  |
          +-------+-------+
          |       |       |
          v       v       v
        Queue   Queue   Queue
          |       |       |
          v       v       v
       Consumer Consumer Consumer
```

El API Gateway continuará gestionando las peticiones HTTP hacia los microservicios. RabbitMQ se utilizará para los flujos de mensajería asíncrona que se definan.

## 5. Configuración actual

RabbitMQ se ejecuta mediante Docker Compose.

### Puertos

| Puerto | Uso |
|---|---|
| `5672` | Comunicación AMQP entre aplicaciones y RabbitMQ. |
| `15672` | Interfaz web RabbitMQ Management. |

### Acceso local

La interfaz de administración está disponible en:

`http://localhost:15672`

Credenciales configuradas para el entorno local de desarrollo:

- **Usuario:** `admin`
- **Contraseña:** `admin123`

Estas credenciales son exclusivamente de desarrollo y deben reemplazarse por credenciales seguras antes de desplegar en un entorno compartido o de producción.

## 6. Ejecución del proyecto

### Requisitos previos

- Docker instalado y funcionando.
- Docker Compose disponible.
- Repositorio clonado localmente.

### Levantar RabbitMQ

Desde la carpeta del proyecto, ejecutar:

```bash
docker compose up -d
```

### Verificar el contenedor

```bash
docker ps
```

### Consultar los registros

```bash
docker compose logs -f rabbitmq
```

### Detener RabbitMQ

```bash
docker compose down
```

El volumen persistente `rabbitmq_data` permite conservar los datos de RabbitMQ cuando se detiene y vuelve a iniciar el contenedor. No se debe eliminar el volumen si se desea conservar esos datos.

## 7. Topic Exchange

Se ha seleccionado **Topic Exchange** porque Cuidado Seguro maneja diferentes eventos relacionados con pacientes y datos clínicos.

Las Routing Keys se organizarán mediante segmentos separados por puntos.

Ejemplos propuestos:

```text
paciente.creado
paciente.actualizado
ficha.creada
evolucion.registrada
signos-vitales.registrados
antropometria.registrada
```

Los siguientes patrones permiten agrupar mensajes:

- `paciente.*`: coincide con una Routing Key de dos segmentos que comienza con `paciente`.
- `ficha.#`: coincide con Routing Keys que comienzan con `ficha` y tienen cero o más segmentos adicionales.
- `#.registrada`: coincide con Routing Keys cuyo último segmento es `registrada`.

Estos nombres son ejemplos iniciales. Los eventos, colas y bindings definitivos se establecerán durante la implementación.

## 8. Plan de implementación

Las siguientes funcionalidades están planificadas y todavía deben implementarse y probarse:

1. Crear el Topic Exchange.
2. Definir las colas y los bindings.
3. Establecer las convenciones de Routing Keys.
4. Implementar un Producer y un Consumer de prueba.
5. Verificar el enrutamiento de mensajes.
6. Incorporar ACK/NACK y mecanismos de reintento.
7. Configurar DLX/DLQ y TTL según los requisitos de la evaluación.
8. Evaluar la persistencia, el prefetch/QoS y las demás medidas de confiabilidad necesarias.
9. Integrar RabbitMQ progresivamente con los microservicios de Cuidado Seguro.

## 9. Estado actual

**Implementado:**

- Repositorio GitHub creado.
- Rama de trabajo `feature/rabbitmq` creada.
- Archivo `docker-compose.yml` configurado.
- Contenedor RabbitMQ levantado correctamente.
- Acceso a RabbitMQ Management comprobado.
- Primer commit de infraestructura realizado.

**Pendiente:**

- Configuración del Topic Exchange.
- Creación de colas y bindings.
- Implementación de productores y consumidores.
- Pruebas de mensajería.
- Integración con los microservicios.

## 10. Estrategia de ramas

El desarrollo se realiza en ramas de funcionalidad para evitar trabajar directamente sobre `main`.

Rama actual de desarrollo:

```text
feature/rabbitmq
```

Los cambios se probarán antes de preparar su integración en `main`.

## 11. Consideraciones de seguridad

- No utilizar credenciales de desarrollo en producción.
- No publicar los puertos de RabbitMQ en Internet sin controles de acceso.
- Gestionar las credenciales mediante variables seguras o un gestor de secretos.
- Restringir los permisos de usuarios y aplicaciones según sus responsabilidades.
- Evitar incluir contraseñas reales en el repositorio.

## 12. Proyecto

**Cuidado Seguro** es una plataforma orientada a la gestión de información clínica de pacientes institucionalizados. RabbitMQ permitirá incorporar comunicación asíncrona entre sus componentes cuando el flujo de negocio lo requiera.