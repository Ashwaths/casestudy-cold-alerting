# Modernization Plan: Nimbus Freight Azure Modernization

**Project**: nimbus-freight

## Technical Framework

- **Language**: Java; primary service targets Java 25, while the assessed
  `bin` project targets Java 21
- **Framework**: Spring Boot 3.5.16
- **Build Tool**: Maven
- **Database**: MySQL 8
- **Key Dependencies**: Spring Security, Spring Data JPA, Spring Mail,
  MySQL Connector/J

## Build Environment

- **Target JDK**: Java 25.0.5.1
- **JDK Path**: `C:\Program Files\Microsoft\jdk-25.0.5.102-hotspot\bin`
- **Maven Path**: `C:\Users\Ashwath Narayana\.maven\maven-3.10.1\bin`
- **Observed Build Tool**: Maven 3.10.1

## Overview

This modernization moves the selected application capabilities toward Azure
services while preserving the existing freight-temperature alerting behavior.
It will:

- Align the assessed Java runtime with the application's Java 25 target.
- Move identity, persistence, secret management, and email capabilities to
  the selected Azure services.
- Review configuration and network communication for cloud-compatible
  endpoints and secure protocols.

The work is ordered as a runtime upgrade followed by the selected service and
configuration transformations, then a dependency-security review. No deployment
or integration-testing task was requested.

## Migration Impact Summary

| App | Current | Target | Auth | Notes |
|---|---|---|---|---|
| Nimbus | Java 21 | Java 25 | N/A | `bin` project |
| Nimbus | Spring Security | Entra ID | Entra ID | User sign-in |
| Nimbus | MySQL | Azure MySQL | Passwordless | Data migration |
| Nimbus | Plaintext secrets | Key Vault | MI | Secrets |
| Nimbus | Jakarta JPA | Azure MySQL | N/A | Persistence |
| Nimbus | JavaMail | Azure Communication Services | N/A | Email |
| Nimbus | Hardcoded URLs | Externalized endpoints | N/A | Config |
| Nimbus | Unsecure protocol | Secure protocols | N/A | Network |
| Nimbus | Localhost | Azure resources | N/A | Runtime dependency |

## Selected Assessment Scope

- Auth Migration (Spring Security) — Migrate from on-premises user authentication to Microsoft Entra ID [kbId: on-premises-user-authentication-to-microsoft-entra-id]
- Database Migration (MySQL) — Migrate to Azure Database for MySQL [kbId: mi-mysql]
- Local Credential — Migrate from Plaintext Credentials to Azure Key Vault [kbId: plaintext-credential-to-azure-keyvault]
- Jakarta Migration (Jakarta Persistence) — Migrate Jakarta JPA to Azure
- Remote Communication (JavaMail) — Migrate to Azure Communication Service [kbId: javax.email-send-to-azure-communication-service-email]
- Remote Communication (Hardcoded Urls) — Check hardcoded URLs
- Remote Communication (Secure Protocols) — Use Secure Protocols
- Local Resource Access (Localhost) — Migrate the Local Resource to Azure
- Java Version Upgrade — Upgrade Java Version [kbId: java-version-upgrade]

## Plan Notes

- The selected assessment findings were confirmed in the report and current
  project files. The Java-version finding is in `bin/pom.xml` (Java 21); the
  primary Maven project already targets Java 25.
- A plan-wide dependency-security review is included as required by the
  modernization planning workflow.
- Transformations are ordered sequentially to avoid concurrent changes to
  shared application configuration. The security review follows them.
- No rulebook was present. No clarification or integration-testing request was
  provided, and no deployment task is included.
