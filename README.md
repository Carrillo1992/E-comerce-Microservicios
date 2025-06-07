# E-commerce con Arquitectura de Microservicios en Java
Este proyecto es una implementación de una plataforma de e-commerce completamente funcional, construida utilizando una arquitectura de microservicios con Spring Boot y Spring Cloud. El sistema está diseñado para ser robusto, escalable y mantenible, separando las responsabilidades de negocio en servicios independientes.

## Arquitectura
El sistema se compone de varios microservicios que se comunican de forma síncrona (REST) y asíncrona (eventos con RabbitMQ), con un API Gateway como único punto de entrada para los clientes.
**Componentes:**
- **API Gateway:** Punto de entrada único para todas las peticiones. Enruta el tráfico a los servicios internos y maneja la autenticación JWT como primera barrera de seguridad.
- **Servicio de Usuarios:** Gestiona todo lo relacionado con los usuarios, incluyendo el registro, login y la generación de tokens JWT.
- **Servicio de Productos:** Gestiona el catálogo de productos y categorías. Escucha eventos para actualizar el stock.
- **Servicio de Pedidos:** Permite a los usuarios crear y consultar sus pedidos. Se comunica con el servicio de productos para validar información y publica un evento cuando se crea un nuevo pedido.
- **RabbitMQ:** Broker de mensajería utilizado para la comunicación asíncrona, desacoplando la creación de pedidos de la actualización de stock.
- **MySQL:** Base de datos relacional. Cada microservicio tiene su propio esquema de base de datos para asegurar la autonomía (users_db, products_db, orders_db).
## Tecnologías Utilizadas
- **Backend:** Java 23, Spring Boot 3.5.0, Spring Security
- **API Gateway:** Spring Cloud Gateway
- **Base de Datos:** Spring Data JPA (Hibernate), MySQL 8.0, H2
- **Mensajería:** Spring AMQP, RabbitMQ
- **Contenedores:** Docker, Docker Compose
- **Build/Dependencias:** Maven
- **Utilidades:** Lombok, JJWT
- **Pruebas:** JUnit 5, Mockito, AssertJ, Spring Test, MockMvc
## Cómo Empezar y Ejecutar el Proyecto
Sigue estos pasos para levantar el entorno completo de desarrollo localmente usando Docker.
### Prerrequisitos
Asegúrate de tener instalado el siguiente software en tu máquina:
- Git
- JDK 23
- Maven 3.8+
- Docker
#### 1. Clonar el Repositorio
```
git clone https://github.com/Carrillo1992/E-comerce-Microservicios.git
```

#### 2. Configuración del Entorno
Este proyecto utiliza un archivo .env para gestionar las variables de entorno, especialmente los secretos.
#### 1- En la raíz del proyecto,crea el archivo .env:

#### 2- Edita el archivo .env y rellena los valores. Un ejemplo de env.example sería:
```
# Puertos expuestos en el host (puedes cambiarlos si están ocupados)
API_GATEWAY_HOST_PORT=8080
MYSQL_HOST_PORT=3307
RABBITMQ_PORT=5672

#BBDD
DB_HOST=mysql-db
MYSQL_DATABASE=users_db
MYSQL_ROOT_PASSWORD=password_seguro_para_root
DB_USER=ecommerce_user
DB_PASSWORD=password_seguro_para_ecommerce_user
DB_NAME_USUARIOS=users_db
DB_NAME_PRODUCTOS=products_db
DB_NAME_PEDIDOS=orders_db
DDL_AUTO=update

# Configuración de JWT (JSON Web Token)
# Clave secreta para firmar los tokens.
JWT_SECRET_KEY=alguna_clave_de_32_bytes
# Tiempo de expiración del token en milisegundos (86400000 ms = 24 horas)
JWT_EXPIRATION_MS=86400000

# Puertos internos de los contenedores
API_GATEWAY_INTERNAL_PORT=8080
USER_SERVICE_INTERNAL_PORT=8081
PRODUCT_SERVICE_INTERNAL_PORT=8082
ORDER_SERVICE_INTERNAL_PORT=8083

# Nombres de servicio para la red Docker
USER_SERVICE_HOST_DOCKER=user-service
PRODUCT_SERVICE_HOST_DOCKER=product-service
ORDER_SERVICE_HOST_DOCKER=order-service
MYSQL_DB_HOST=mysql-db
RABBITMQ_HOST=rabbitmq-server
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest

# Direcciones completas que el API Gateway usará para redirigir las peticiones.
USER_SERVICE_URI=http://user-service:8081
PRODUCT_SERVICE_URI=http://product-service:8082
ORDER_SERVICE_URI=http://order-service:8083

```

#### 3. Construir los JARs de los Microservicios
Antes de levantar los contenedores, necesitas empaquetar cada aplicación Spring Boot en un archivo JAR.

Desde la raíz de cada servicio usar el comando:
```
mvn clean package
```

Si los tests fallan por configuración, puedes saltarlos temporalmente para la dockerización con:

```
mvn clean package -DskipTests
```

#### 4. Levantar el Entorno con Docker Compose
Este comando construirá las imágenes Docker para cada servicio (si es la primera vez o si hubo cambios) y levantará todos los contenedores definidos en docker-compose.yml.
Desde la raíz ejecuta el comando:
```
docker-compose up --build
```
### Documentación de la API
Toda la interacción con la API se realiza a través del API Gateway en http://localhost:8080.

**Autenticación (user-service)** 

**POST /api/v1/auth/register**

Registra un nuevo usuario.
- **Autenticación:** Pública.
- **Request Body:**
```
{
  "name": "Nuevo Usuario",
  "email": "usuario@example.com",
  "password": "password123"
}
```
- **Success Response (201 CREATED):**
- 
"Usuario registrado exitosamente!"

**POST /api/v1/auth/login**

**Autentica un usuario y devuelve un token JWT.**

- **Autenticación:** Pública.
- **Request Body:**
```
{
  "email": "usuario@example.com",
  "password": "password123"
}
```

- **Success Response (200 OK):**
```
{
  "accessToken": "ey...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "usuario@example.com",
  "roles": ["ROLE_USER"]
}
```

**Usuarios (user-service)**

**GET /api/v1/users/me**
Obtiene el perfil del usuario autenticado.

- **Autenticación:** Requiere token de usuario (Bearer Token).
- **Success Response (200 OK):**
```
{
  "id": 1,
  "name": "Nuevo Usuario",
  "email": "usuario@example.com",
  "roles": ["ROLE_USER"]
}
```

**Productos (product-service)**
**GET /api/v1/products**
Lista los productos de forma paginada.
- **Autenticación:** Pública.
- Query Params: ?page=0&size=10&sort=name,asc
Success Response (200 OK): Devuelve un objeto Page con la lista de productos.
POST /api/v1/products
Crea un nuevo producto.
Autenticación: Requiere token de ADMIN (ROLE_ADMIN).
Request Body:
{
  "name": "Producto Nuevo",
  "description": "Descripción del producto.",
  "price": 19.99,
  "stock": 100,
  "categoryId": 1
}


- **Success Response (201 CREATED):** Devuelve el DTO del producto creado.
- 
**Pedidos (order-service)**
**POST /api/v1/orders**

Crea un nuevo pedido.
- **Autenticación:** Requiere token de usuario. 
- **Request Body:**
```
{
  "address": "Calle Principal 123, Madrid",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

- **Success Response (201 CREATED):** Devuelve el DTO del pedido creado.
  
**GET /api/v1/orders**

Obtiene el historial de pedidos del usuario autenticado.
- **Autenticación:** Requiere token de usuario.
- **Query Params:** ?page=0&size=10
- **Success Response (200 OK):** Devuelve un objeto Page con la lista de pedidos del usuario. 
### Pruebas
Para ejecutar las pruebas unitarias y de integración, usa el siguiente comando Maven en la raíz del cada servicio:
```
mvn test
```

