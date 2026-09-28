# Authentication System — JWT + OTP Verification

A secure authentication system built with **Spring Boot**, implementing **JWT-based authentication**, **Access & Refresh Tokens**, **OTP email verification**, password hashing, secure HTTP-only cookies, and MySQL persistence.

## Features

* User registration with validation
* BCrypt password hashing
* Email OTP verification
* Secure 6-digit OTP generation using `SecureRandom`
* Hashed OTP storage
* OTP expiry after 5 minutes
* OTP resend functionality
* Maximum 3 OTP resend attempts
* Old OTP invalidation when a new OTP is generated
* JWT Access Token with 15-minute expiry
* JWT Refresh Token with 7-day expiry
* JWT type validation (`access` / `refresh`)
* JWT authentication through Spring Security
* HTTP-only cookies for token storage
* Protected authenticated endpoints
* Login restricted to verified users
* Global exception handling
* Meaningful HTTP status codes
* MySQL database integration
* Gmail SMTP email integration

## Technologies Used

* **Java 25**
* **Spring Boot 4.1.1**
* Spring Web
* Spring Data JPA
* Spring Security
* Spring Validation
* Spring Mail
* MySQL
* JSON Web Token (JWT)
* JJWT (Java JWT) library
* Maven
* Postman

## Project Structure

```text
src/main/java/com/codewithsvns/authentication/
│
├── controller/
│   ├── AuthController.java
│   └── UserController.java
│
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   ├── RegisterResponse.java
│   ├── ResendOtpRequest.java
│   └── VerifyOtpRequest.java
│
├── entity/
│   └── User.java
│
├── exception/
│   ├── BadRequestException.java
│   ├── ConflictException.java
│   ├── GlobalExceptionHandler.java
│   ├── NotFoundException.java
│   └── TooManyRequestsException.java
│
├── repository/
│   └── UserRepository.java
│
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   └── SecurityConfig.java
│
└── service/
    ├── AuthService.java
    ├── EmailService.java
    └── OtpService.java
```

## Authentication Flow

### 1. Registration

```text
Client
  ↓
POST /api/auth/register
  ↓
Validate request
  ↓
Check email uniqueness
  ↓
Hash password using BCrypt
  ↓
Generate secure OTP
  ↓
Hash OTP using BCrypt
  ↓
Store OTP + 5-minute expiry
  ↓
Send OTP through email
  ↓
Create unverified user
```

The user is **not logged in** after registration.

### 2. OTP Verification

```text
Client
  ↓
POST /api/auth/verify-otp
  ↓
Find user by email
  ↓
Check OTP expiry
  ↓
Compare OTP with stored BCrypt hash
  ↓
Mark user as verified
  ↓
Invalidate OTP
```

### 3. Login

```text
Client
  ↓
POST /api/auth/login
  ↓
Validate credentials
  ↓
Check email verification
  ↓
Generate Access Token
  ↓
Generate Refresh Token
  ↓
Store tokens in HTTP-only cookies
```

### 4. Accessing Protected Resources

```text
Client Request
     ↓
accessToken cookie
     ↓
JwtAuthenticationFilter
     ↓
Validate JWT signature + expiry + token type
     ↓
Extract user email
     ↓
Create Authentication object
     ↓
SecurityContext
     ↓
Protected Controller
```

### 5. Refreshing the Access Token

The access token expires after 15 minutes.

The refresh token remains valid for 7 days.

```text
Refresh Token
     ↓
POST /api/auth/refresh-token
     ↓
Validate refresh token
     ↓
Check token type
     ↓
Check user exists and is verified
     ↓
Generate new Access Token
```

## API Endpoints

### Register

**POST** `/api/auth/register`

Request:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password123"
}
```

Response:

**201 Created**

```json
{
  "message": "Registration successful. Please verify your email with the OTP."
}
```

---

### Verify OTP

**POST** `/api/auth/verify-otp`

Request:

```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

Response:

**200 OK**

```text
Email verified successfully.
```

---

### Resend OTP

**POST** `/api/auth/resend-otp`

Request:

```json
{
  "email": "john@example.com"
}
```

Response:

**200 OK**

```text
A new OTP has been sent to your email.
```

Maximum resend attempts: **3**

After the limit:

**429 Too Many Requests**

---

### Login

**POST** `/api/auth/login`

Request:

```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```

Response:

**200 OK**

```text
Login successful
```

The server sets:

* `accessToken` — HTTP-only cookie, 15-minute expiry
* `refreshToken` — HTTP-only cookie, 7-day expiry

The token values are not returned in the response body.

---

### Refresh Access Token

**POST** `/api/auth/refresh-token`

The refresh token is read from the HTTP-only `refreshToken` cookie.

Response:

**200 OK**

```text
Access token refreshed successfully
```

A new `accessToken` cookie is issued.

---

### Logout

**POST** `/api/auth/logout`

Response:

**200 OK**

```text
Logout successful
```

The `accessToken` and `refreshToken` cookies are cleared.

---

### Protected User Endpoint

**GET** `/api/user/me`

Requires a valid access token.

Response:

**200 OK**

```text
Authenticated user: john@example.com
```

## Security

### Password Hashing

Passwords are never stored as plain text.

The application uses **BCrypt**:

```text
Plain Password
      ↓
BCrypt
      ↓
Password Hash
      ↓
Database
```

During login, the supplied password is compared against the stored hash using BCrypt.

### OTP Security

OTPs are generated using Java's `SecureRandom` and are also hashed using BCrypt before being stored.

Only the original OTP is sent to the user's email.

The stored OTP hash cannot directly reveal the OTP.

### JWT Security

Each JWT contains a token type:

```text
type = access
```

or:

```text
type = refresh
```

Access and refresh tokens therefore cannot be used interchangeably.

JWT expiration is also validated before authentication is established.

### Token Storage

Tokens are stored in **HTTP-only cookies**, preventing client-side JavaScript from directly reading them.

## Database

The application uses **MySQL** with Spring Data JPA.

The `User` entity stores:

* User ID
* Name
* Email
* BCrypt password hash
* Verification status
* Hashed OTP
* OTP expiry
* OTP resend count

## Configuration

Sensitive credentials are supplied through environment variables rather than being hard-coded.

Example:

```properties
spring.datasource.username=${AUTH_USERNAME}
spring.datasource.password=${AUTH_PASSWORD}

spring.mail.username=${AUTH_MAIL_USERNAME}
spring.mail.password=${AUTH_MAIL_PASSWORD}

jwt.secret=${JWT_SECRET}
```

Before deploying, configure the required environment variables on the hosting platform.

## Testing

The API was tested using **Postman**.

The following scenarios were tested:

* Successful registration
* OTP verification
* Invalid OTP
* OTP resend
* OTP resend limit
* Duplicate email registration
* Invalid request validation
* Successful login
* Incorrect password
* Login before email verification
* Access token refresh
* Logout
* Protected endpoint authentication

## HTTP Status Codes

| Status                  | Usage                                     |
| ----------------------- | ----------------------------------------- |
| `200 OK`                | Successful operations                     |
| `201 Created`           | Successful registration                   |
| `400 Bad Request`       | Invalid input, OTP, credentials, or token |
| `404 Not Found`         | User not found                            |
| `409 Conflict`          | Email already registered                  |
| `429 Too Many Requests` | OTP resend limit exceeded                 |

## Running Locally

### 1. Clone the repository

```bash
git clone https://github.com/shivansh-rgb-code/authentication-system
cd authentication-system
```

### 2. Configure environment variables

Set:

```text
AUTH_USERNAME
AUTH_PASSWORD
AUTH_MAIL_USERNAME
AUTH_MAIL_PASSWORD
JWT_SECRET
```

### 3. Create the MySQL database

Create a database named:

```text
authentication_system
```

### 4. Run the application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

## Author

**Shivansh Srivastava**
