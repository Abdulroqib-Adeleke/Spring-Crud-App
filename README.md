# Spring CRUD App

A Spring Boot-based RESTful CRUD application for managing employees. This project demonstrates clean architecture with DTOs, service-layer business rules, exception handling, and file import/export capabilities (Excel & PDF).

---

## 🚀 Features

- Create, Read, Update, Delete (CRUD) Employee records
- Duplicate email validation
- Salary validation rules
- Partial updates (PATCH)
- Global exception handling
- Import employees via Excel file
- Export employee data to Excel and PDF
- H2 in-memory database

---

## 🏗️ Tech Stack

- Java 21 LTS
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database
- Apache POI (Excel processing)
- Itext (PDF processing)
- Maven

---

## 📁 Project Structure

```
src/main/java/com/SpringCrudApp/crudApp
│
├── controller          # REST controllers
├── dto                 # Request/Response DTOs
├── exception           # Custom exceptions & handler
├── model               # Entity classes
├── repository          # JPA repositories
├── service             # Service interfaces
├── service/impl        # Business logic implementations
└── CrudAppApplication  # Main class
```

---

## ⚙️ Setup & Run

### 1. Clone the Repository
```
git clone https://github.com/Abdulroqib-Adeleke/Spring-Crud-App.git
cd Spring-Crud-App
```

### 2. Build the Project
```
mvn clean install
```

### 3. Run the Application
```
mvn spring-boot:run
```

Application runs at:
```
http://localhost:8080
```

---

## 🗄️ Database

- H2 in-memory database
- Console available at:
```
http://localhost:8080/h2-console
```

Default config (from `application.properties`):
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: *(empty)*

---

## 📌 API Endpoints

### Employee CRUD

| Method | Endpoint                     | Description                                      |
|--------|------------------------------|--------------------------------------------------|
| POST   | `/api/v1/employees`          | Create employee                                  |
| GET    | `/api/v1/employees`          | Get all employees                                |
| GET    | `/api/v1/employees/{id}`     | Get employee by ID                               |
| PUT    | `/api/v1/employees/{id}`     | Update employee                                  |
| PATCH  | `/api/v1/employees/{id}`     | Partial update                                   |
| DELETE | `/api/v1/employees/{id}`     | Set employee active to false                     |
| DELETE | `/api/v1/employees/{id}/hard` | Delete employee from database if active is false |
| GET    | `/api/v1/employees/salary-range` | Filter employee by salary range                  |
| POST   | `/api/v1/employees/import`   | Import employee from excel file (.xlsx)          |
| GET    | `/api/v1/employees/export/excel` | Export employee data as excel file               |
| GET    | `/api/v1/employees/export/pdf` | Export employee in PDF file format               |

---

## 📥 Excel Import

**Endpoint:**
```
POST /api/v1/employees/import
```

**Request:**
- `multipart/form-data`
- field name: `file`

### Expected Excel Format

| Column | Field |
|-------|------|
| A | firstName |
| B | lastName |
| C | email |
| D | department |
| E | salary |
| F | dateOfJoining (YYYY-MM-DD) |
| G | active (TRUE/FALSE) |

### Response

```json
{
  "successCount": 10,
  "failureCount": 2,
  "errors": ["Row 3: Invalid email"]
}
```

---

## 📤 Export

### Export to Excel
```
GET /api/v1/employees/export/excel
```

### Export to PDF
```
GET /api/v1/employees/export/pdf
```

---

## ⚠️ Business Rules

- Email must be unique
- Salary must meet minimum constraints
- Invalid file formats throw exceptions
- Proper validation is handled in service layer


---

## 📌 Error Handling

Custom exceptions:

- `DuplicateEmailException`
- `EmployeeNotFoundException`
- `ExcelProcessingException`
- `InvalidFileFormatException`

Handled globally via:
```
GlobalExceptionHandler
```

---

## 👨‍💻 Author

Adeleke Abdulroqib Ayomiposi

