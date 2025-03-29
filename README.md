**Introduction**

This project represents the backend of a car-sharing service, developed to demonstrate my skills in building various API solutions. The system implements the complete car rental lifecycle with role-based functionality:

**Roles and Capabilities:**

1. **Manager (Admin)**
- Full fleet management (add/edit/delete vehicles)
- User permissions management (assign/modify roles)
- Rental control:
    - View active rentals
    - Handle overdue rentals (with Telegram notifications)
- Payment system monitoring

2. **Customer (User)**
- Personal account:
    - View and edit profile
- Booking:
    - Select and rent vehicles
    - Payment via integrated payment system

3. **Guest (Unauthorized)**
- Browse available vehicle catalog

---

**Technology Stack**

The project leverages the following modern technologies and tools:

**Core Technologies:**
- **Spring Boot** - Backend application framework
- **Spring Security** - Secure authentication and authorization
- **JWT** - Stateless authentication mechanism

**Data Layer:**
- **Spring Data JPA** - ORM and database abstraction
- **MySQL** - Relational database management system
- **Liquibase** - Database migration and version control

**Infrastructure & DevOps:**
- **Docker** - Containerization and deployment
- **Docker Compose** - Multi-container orchestration

**Testing:**
- **Testcontainers** - Integration testing with real dependencies
- **JUnit** - Unit and integration testing framework
- **Mockito** - Mocking framework for tests

**API & Documentation:**
- **OpenAPI** - API documentation and testing

**Payment Integration:**
- **Stripe API** - Payment processing system

**Notification Service:**
- **Telegram Bot API** - Real-time rental notifications

**Build & Dependency Management:**
- **Maven** - Project build automation
- **Lombok** - Code reduction boilerplate

---

**System Functionality**

The application implements the following core modules with comprehensive functionality:

**1. Authentication Module (AuthController)**
- User registration
- JWT-based authentication

**2. User Management (UserController)**
- Role-based access control:
    - Assign/revoke user roles (Manager/Customer)
- Profile management:
    - Get current user profile
    - Update user information

**3. Vehicle Management (CarController)**
- Complete CRUD operations:
    - Add new vehicles to inventory
    - Retrieve all available vehicles
    - Search/filter vehicles
    - Update vehicle specifications
    - Remove vehicles from system

**4. Rental Management (RentalController)**
- End-to-end rental processing:
    - Initiate rental contract
    - Close rental session
    - View rental history
- Advanced features:
    - Rental duration tracking
    - Overdue rental handling

**5. Payment Processing (PaymentController)**
- Integrated payment flow:
    - Secure payment processing
- Transaction management:
    - Payment history
    - Invoice management

---

**Installation and Setup**  
To run this project locally, follow these steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/retkinf/car-sharing-service
   ```

2. **Navigate to the project folder**:
   ```bash
   cd car-sharing-service
   ```

3. **Set up environment variables**:
    - Create a `.env` file in the root directory of the project.
    - Add the following environment variables to the `.env` file:
      ```bash
       MYSQLDB_ROOT_PASSWORD=
       MYSQLDB_DATABASE=
       MYSQLDB_USER=
       MYSQLDB_PASSWORD=
       MYSQLDB_LOCAL_PORT=
       MYSQLDB_DOCKER_PORT=
       SPRING_LOCAL_PORT=
       SPRING_DOCKER_PORT=
       DEBUG_PORT=
       STRIPE_API_KEY=
       TELEGRAM_BOT_TOKEN=
       TELEGRAM_BOT_USERNAME=
       TELEGRAM_CHAT_ID=
      ```

4. **Install dependencies using Maven**:
   ```bash
   mvn clean install
   ```

5. **Run the application using Docker**:
    - Ensure Docker is installed and running on your machine.
    - Build and run the Docker container using the following command:
      ```bash
      docker-compose up --build
      ```

6. **Access the application**:
    - Once the container is running, the application will be accessible at `http://localhost:8080`.

---

**Usage**

After launching the application, you can access the API via Swagger UI at the following address:  
[http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html)

For convenient testing, I have created a Postman collection that you can import and use to test the API. The collection includes example requests for all available endpoints. Here is the link to the collection:  
[https://www.postman.com/orbital-module-candidate-80082765/workspace/car-sharing-service/collection/34994240-3d7a6a0e-7862-49cc-a7cc-add478bf4d1b?action=share&creator=34994240](https://www.postman.com/orbital-module-candidate-80082765/workspace/car-sharing-service/collection/34994240-3d7a6a0e-7862-49cc-a7cc-add478bf4d1b?action=share&creator=34994240)
