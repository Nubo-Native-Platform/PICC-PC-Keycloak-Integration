# Development Guidelines and Contribution Standards: `PICC-PC-Keycloak-Integration`

This document defines the architectural standards, development workflows, coding conventions, and security requirements for contributors to **`PICC-PC-Keycloak-Integration`** within the **Nubo Native Platform (NNP)**.

---

## Table of Contents

1. [Architecture & Design Principles](#1-architecture--design-principles)
2. [Project & Package Structure](#2-project--package-structure)
3. [Development Environment Setup](#3-development-environment-setup)
4. [Keycloak Integration Mechanics](#4-keycloak-integration-mechanics)
   - [Feign Declarative Clients](#feign-declarative-clients)
   - [Automated Token Interception & Renewal](#automated-token-interception--renewal)
   - [Token Caching Layer](#token-caching-layer)
   - [Custom Error Decoding](#custom-error-decoding)
5. [Security & Sanitization Guidelines](#5-security--sanitization-guidelines)
   - [Mitigating Log Injection (CWE-117)](#mitigating-log-injection-cwe-117)
   - [Credential & Secret Hygiene](#credential--secret-hygiene)
6. [Code Quality, SAST & SBOM Tooling](#6-code-quality-sast--sbom-tooling)
   - [SpotBugs & FindSecBugs (SAST)](#spotbugs--findsecbugs-sast)
   - [OWASP Dependency-Check (SCA)](#owasp-dependency-check-sca)
   - [CycloneDX SBOM Generation (CNCF Supply Chain)](#cyclonedx-sbom-generation-cncf-supply-chain)
   - [Checkstyle (Google Java Style)](#checkstyle-google-java-style)
7. [Testing & Verification Strategy](#7-testing--verification-strategy)
8. [Open-Source Contribution Guidelines](#8-open-source-contribution-guidelines)

---

## 1. Architecture & Design Principles

The Keycloak Integration Service acts as an anti-corruption and abstraction layer between enterprise microservices/frontends and the Keycloak Identity and Access Management (IAM) server.

### Core Architectural Principles

- **Declarative HTTP Binding**: Utilizing Spring Cloud OpenFeign to map Keycloak Admin REST APIs directly to Java interfaces.
- **Transparent Token Lifecycle**: Delegating JWT bearer authentication, expiry validation, and transparent refresh token exchanges to a dedicated Feign `RequestInterceptor`, keeping controller and service methods completely decoupled from authentication plumbing.
- **Fail-Fast Error Decoding**: Decoding Keycloak HTTP errors (e.g. 401 Unauthorized, 404 Realm Not Found, 409 Conflict) into typed `KeycloakIntegrationException` instances with structured error messages.
- **Supply-Chain & DevSecOps Readiness**: Automated static analysis (SpotBugs), dependency vulnerability auditing (OWASP), and immutable SBOM generation (CycloneDX) embedded in the Maven build pipeline.

---

## 2. Project & Package Structure

```
PICC-PC-Keycloak-Integration/
├── .env.example                               # Environment variable blueprint
├── .github/
│   └── workflows/
│       └── ci-cd.yml                          # GitHub Actions CI/CD pipeline
├── .gitignore                                 # Git exclusion rules (secrets, logs, build)
├── application.properties.example             # Comprehensive configuration reference
├── docker-compose.yml                         # Local Docker orchestration (Keycloak + App)
├── Dockerfile                                 # Multi-stage unprivileged container specification
├── LICENSE                                    # Apache License 2.0
├── pom.xml                                    # Maven build, dependencies & security plugins
├── README.md                                  # Project overview, architecture & quickstart
├── DEVELOPMENT_GUIDELINES.md                  # Detailed developer & contribution guide
├── USER_MANUAL_AND_DEPLOYMENT_GUIDE.md        # User manual & production deployment runbook
├── spotbugs-exclude.xml                       # Filter configuration for SpotBugs SAST
├── docs/
│   └── images/
│       ├── architecture.svg                   # System architecture diagram
│       └── workflow.svg                       # Token interception sequence diagram
└── src/
    ├── main/
    │   ├── java/com/nnp/keycloak/
    │   │   ├── KeycloakIntegrationApplication.java   # Spring Boot entrypoint
    │   │   ├── config/
    │   │   │   ├── CacheConfig.java                  # Spring Cache configuration
    │   │   │   ├── KeycloakConfig.java               # Feign RequestInterceptor & OAuth2 logic
    │   │   │   ├── KeycloakCustomErrorDecoder.java   # Feign ErrorDecoder for Keycloak
    │   │   │   ├── ModelMapperConfig.java            # DTO mapping configuration
    │   │   │   └── OpenApiConfig.java                # OpenAPI 3 / Swagger metadata
    │   │   ├── controller/
    │   │   │   └── KeycloakIntegrationController.java # 30 REST endpoints
    │   │   ├── exception/
    │   │   │   ├── KeycloakExceptionHandler.java     # @RestControllerAdvice
    │   │   │   ├── KeycloakExceptionMessage.java
    │   │   │   └── KeycloakIntegrationException.java
    │   │   ├── model/                                # Keycloak DTOs & payloads
    │   │   │   ├── AuthTokenRequest.java
    │   │   │   ├── ClientCreationRequest.java
    │   │   │   ├── ClientDetails.java
    │   │   │   ├── CreateRealmRequest.java
    │   │   │   ├── KeycloakAccessToken.java
    │   │   │   ├── UserCreation.java
    │   │   │   ├── UserDetails.java
    │   │   │   └── UserUpdate.java
    │   │   ├── rest/model/                           # REST View Objects (VOs)
    │   │   ├── service/
    │   │   │   ├── KeycloakClientService.java        # High-level orchestration & replication
    │   │   │   ├── cache/CachingService.java         # Cache eviction & access
    │   │   │   └── feign/
    │   │   │       ├── KeycloakAdminClient.java      # Declarative Keycloak Admin client
    │   │   │       └── KeycloakClient.java           # Keycloak Token endpoint client
    │   │   └── utils/
    │   │       └── LogUtils.java                     # CRLF Log Injection mitigation
    │   └── resources/
    │       └── application.properties                # Base runtime properties
    └── test/
        └── java/com/nnp/keycloak/
            └── KeycloakIntegrationApplicationTests.java
```

---

## 3. Development Environment Setup

### Prerequisites

- **Java Development Kit (JDK)**: Version 21 (Eclipse Temurin, OpenJDK, or Oracle)
- **Maven**: Version 3.9+ (or use the included `mvnw` / `mvnw.cmd` wrapper)
- **Docker & Docker Compose**: Version 24+
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse

### Local Setup Steps

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Nubo-Native-Platform/PICC-PC-Keycloak-Integration.git
   cd PICC-PC-Keycloak-Integration
   ```

2. **Start Backing Keycloak Server**:
   ```bash
   docker-compose up -d keycloak
   ```
   Keycloak will be accessible at [http://localhost:8081](http://localhost:8081) with credentials `admin` / `admin`.

3. **Verify Build with Maven Wrapper**:
   ```powershell
   .\mvnw.cmd clean compile
   ```

4. **Launch Application Locally**:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to interact with the API.

---

## 4. Keycloak Integration Mechanics

### Feign Declarative Clients

All administrative endpoints are mapped via `@FeignClient` in `KeycloakAdminClient.java`:

```java
@FeignClient(name = "keycloakAdminClient", url = "${keycloak.url}", configuration = KeycloakConfig.class)
public interface KeycloakAdminClient {
    @PostMapping(path = "/admin/realms/{realmName}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    void createUser(@RequestBody UserCreation userCreationRequest, @PathVariable("realmName") String realmName);
    
    @GetMapping(path = "/admin/realms/{realmName}/clients")
    List<ClientDetails> getAllClientsByRealmName(@PathVariable("realmName") String realmName);
}
```

### Automated Token Interception & Renewal

The `RequestInterceptor` bean configured in `KeycloakConfig.java` intercepts every outgoing Feign request:
1. Inspects the target URI. If it targets the token endpoint (`/protocol/openid-connect/token`), it skips authorization.
2. Checks whether a cached access token exists and verifies that its expiration timestamp has not passed using `com.auth0.jwt.JWT.decode()`.
3. If valid, attaches `Authorization: Bearer <token>` to the outgoing request headers.
4. If expired or missing, initiates a refresh token request (`grant_type=refresh_token`). If refresh fails, performs a client credentials exchange (`grant_type=client_credentials`) against the `master` realm.

### Custom Error Decoding

`KeycloakCustomErrorDecoder.java` inspects HTTP 4xx/5xx responses returned by Keycloak:
- Extracts the error response body.
- Throws an appropriate `KeycloakIntegrationException` with descriptive context.
- Global handling is unified in `KeycloakExceptionHandler.java` (`@RestControllerAdvice`).

---

## 5. Security & Sanitization Guidelines

### Mitigating Log Injection (CWE-117)

Log injection (CRLF injection) occurs when untrusted user input contains carriage return (`\r`) or line feed (`\n`) characters that manipulate log files.

**Rule**: All parameters logged from incoming requests MUST pass through `LogUtils.sanitizeForLog()`:

```java
// Correct
log.debug("Processing request for realm: {}", LogUtils.sanitizeForLog(realmName));
log.info("Updating user ID: {}", LogUtils.sanitizeForLog(userId));

// Incorrect - Flagged by FindSecBugs
log.info("Updating user: " + userId);
```

Implementation in `LogUtils.java`:
```java
public class LogUtils {
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[\r\n]", "_");
    }
}
```

### Credential & Secret Hygiene

- **Never commit `.env` or `application.properties` with plain-text secrets.**
- Use `.env.example` and `application.properties.example` for public documentation.
- All secrets (`keycloak.client_secret`, database passwords, etc.) must be passed via environment variables or secret managers in production.

---

## 6. Code Quality, SAST & SBOM Tooling

The project pom incorporates enterprise-grade DevSecOps plugins:

### SpotBugs & FindSecBugs (SAST)

Executes static analysis for bugs, code smells, and security vulnerabilities:

```powershell
.\mvnw.cmd spotbugs:check
```

- Filter configurations are stored in `spotbugs-exclude.xml`.
- To inspect HTML reports: `target/spotbugsXml.xml`.

### OWASP Dependency-Check (SCA)

Scans third-party dependencies against the National Vulnerability Database (NVD):

```powershell
.\mvnw.cmd dependency-check:check
```

- Configured to fail the build if a vulnerability with CVSS $\ge 7$ (High/Critical) is detected.
- Report generated at `target/dependency-check-report.html`.

### CycloneDX SBOM Generation (CNCF Supply Chain)

Generates a machine-readable Software Bill of Materials in CycloneDX 1.5 JSON format:

```powershell
.\mvnw.cmd cyclonedx:makeAggregateBom
```

- Output file: `target/bom.json`.
- Required for CNCF / CINUM software supply chain verification.

### Checkstyle (Google Java Style)

Verifies formatting and style standards:

```powershell
.\mvnw.cmd checkstyle:check
```

---

## 7. Testing & Verification Strategy

### Running Unit & Integration Tests

```powershell
.\mvnw.cmd test
```

### Building the Full Production Artifact

```powershell
.\mvnw.cmd clean package -DskipTests
```

### Validating Docker Image Build

```bash
docker build -t picc-pc-keycloak-integration:latest .
```

---

## 8. Open-Source Contribution Guidelines

We welcome contributions from the community! To ensure high quality, please follow these conventions:

### Branching Strategy

- `main`: Production-ready code.
- `feature/<feature-name>`: New capabilities or endpoints.
- `fix/<bug-name>`: Bug fixes and security patches.

### Commit Message Conventions

Follow the Conventional Commits standard:
- `feat: add endpoint for bulk client export`
- `fix: correct token refresh retry counter in interceptor`
- `docs: update deployment guidelines for Kubernetes 1.30`
- `security: sanitize user input in group role assignment`

### Pull Request Checklist

Before submitting a PR:
1. Ensure `.\mvnw.cmd clean compile` succeeds with zero errors or warnings.
2. Run `.\mvnw.cmd test` and verify all tests pass.
3. Confirm that no secrets or environment files (`.env`, `*.local.properties`) are tracked.
4. Ensure all new public REST endpoints include OpenAPI annotations (`@Operation`, `@ApiResponses`, `@Parameter`).
