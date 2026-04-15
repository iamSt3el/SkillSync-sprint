# Spring Boot

## What is Spring Boot?

Spring Boot is an opinionated framework built on top of Spring that eliminates boilerplate configuration. It lets you create production-ready Spring applications with minimal setup.

**Core philosophy:** Convention over Configuration — sensible defaults out of the box, override only what you need.

---

## Key Features

| Feature | What it does |
|---|---|
| Auto-configuration | Automatically configures beans based on what's on the classpath |
| Starter POMs | Curated dependency bundles (spring-boot-starter-web, etc.) |
| Embedded server | Tomcat/Jetty/Undertow bundled — no WAR deployment needed |
| Actuator | Production-ready endpoints (health, metrics, info) |
| Spring Initializr | Project scaffolding at start.spring.io |

---

## @SpringBootApplication

The entry point annotation — it is a shortcut for three annotations:

```java
@SpringBootApplication
// is equivalent to:
@Configuration          // this class can define @Bean methods
@EnableAutoConfiguration // turn on auto-configuration
@ComponentScan          // scan current package and sub-packages for beans
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

---

## Auto-Configuration

Spring Boot looks at:
- What's on the classpath (which JARs)
- What properties are set
- What beans already exist

And configures beans automatically.

**Example:** If `spring-boot-starter-data-jpa` is on the classpath and `spring.datasource.url` is configured, Spring Boot auto-creates a `DataSource`, `EntityManagerFactory`, and `TransactionManager` — you write zero configuration for this.

**How it works internally:**
- `spring.factories` / `AutoConfiguration.imports` file lists all auto-configuration classes
- Each class is annotated with `@ConditionalOn...` so it only activates under certain conditions

```java
@ConditionalOnClass(DataSource.class)        // only if JPA is on classpath
@ConditionalOnMissingBean(DataSource.class)  // only if no DataSource bean already defined
public class DataSourceAutoConfiguration { ... }
```

**You can exclude auto-configuration:**
```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
```

---

## Starter POMs

A starter is a curated list of dependencies:

| Starter | What it includes |
|---|---|
| `spring-boot-starter-web` | Spring MVC, Tomcat, Jackson |
| `spring-boot-starter-data-jpa` | Hibernate, Spring Data, JDBC |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-amqp` | RabbitMQ, Spring AMQP |
| `spring-boot-starter-actuator` | Health, metrics, info endpoints |
| `spring-boot-starter-mail` | JavaMailSender |
| `spring-cloud-starter-netflix-eureka-client` | Eureka client registration |
| `spring-cloud-starter-openfeign` | Declarative HTTP clients |

---

## application.yml / application.properties

Spring Boot reads configuration from these files automatically:

```yaml
spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  rabbitmq:
    host: localhost
    port: 5672

server:
  port: 8081

jwt:
  secret: mysecret
  expiration: 86400000
```

**Property resolution order (highest to lowest priority):**
1. Command-line args (`--server.port=9090`)
2. Environment variables (`SERVER_PORT=9090`)
3. `application-{profile}.yml`
4. `application.yml`
5. Defaults

---

## Profiles

Run different config per environment:

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_dev

# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/auth_prod
```

Activate: `spring.profiles.active=prod` or `SPRING_PROFILES_ACTIVE=prod`

---

## Spring Boot Actuator

Exposes production-ready management endpoints:

| Endpoint | URL | What it shows |
|---|---|---|
| Health | `/actuator/health` | UP/DOWN, DB status, disk space |
| Metrics | `/actuator/metrics` | JVM, HTTP, custom metrics |
| Prometheus | `/actuator/prometheus` | Metrics in Prometheus format |
| Info | `/actuator/info` | App metadata |
| Env | `/actuator/env` | All properties |
| Beans | `/actuator/beans` | All Spring beans |

**In SkillSync:** All services expose `/actuator/health` for Kubernetes readiness/liveness probes, and `/actuator/prometheus` for Prometheus scraping.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,metrics
  endpoint:
    health:
      show-details: always
```

---

## Embedded Server

No need to deploy a WAR to an external Tomcat. Spring Boot packages everything into an executable JAR:

```bash
java -jar auth-service.jar
```

The JAR contains Tomcat (or Jetty/Undertow) inside it. In SkillSync, your Dockerfiles do exactly this.

---

## SpringApplication

The `main` method bootstraps the application:

```java
SpringApplication app = new SpringApplication(AuthServiceApplication.class);
app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
app.run(args);
```

**What it does:**
1. Creates the ApplicationContext
2. Registers a shutdown hook
3. Runs `ApplicationRunner` / `CommandLineRunner` beans
4. Starts the embedded server

---

## CommandLineRunner / ApplicationRunner

Run code on startup after the context is loaded:

```java
@Component
public class DataSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // seed initial skills into DB
    }
}
```

---

## Externalized Configuration with @ConfigurationProperties

Bind a group of related properties to a POJO:

```java
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtProperties {
    private String secret;
    private long expiration;
    // getters + setters
}
```

```yaml
jwt:
  secret: mysecret
  expiration: 86400000
```

Cleaner than multiple `@Value` annotations.

---

## DevTools

`spring-boot-devtools` enables hot reload during development — restarts the app automatically when classes change. Used in SkillSync's services during local dev.

---

## Fat JAR vs Thin JAR

- **Fat JAR** — all dependencies bundled inside (what Spring Boot produces). Self-contained, easy to deploy.
- **Thin JAR** — only your code. Dependencies fetched at runtime. Smaller image but more complex.

In SkillSync you use fat JARs built by Maven and containerized with Docker.

---

## Common Interview Questions

**Q: What is auto-configuration and how does it work?**
A: Spring Boot scans the classpath and property files, then activates pre-written @Configuration classes conditionally (using @ConditionalOnClass, @ConditionalOnMissingBean etc.). For example, if JPA jar is present and datasource url is set, it auto-creates EntityManagerFactory without any explicit config.

**Q: What does @SpringBootApplication do?**
A: It combines @Configuration + @EnableAutoConfiguration + @ComponentScan. Single annotation to bootstrap a Spring Boot app.

**Q: Difference between Spring and Spring Boot?**
A: Spring is the framework (IoC, DI, MVC, etc.). Spring Boot is an opinionated layer on top that adds auto-configuration, starter POMs, embedded servers, and Actuator. Spring Boot reduces setup time; you write code not configuration.

**Q: What is a Spring Boot starter?**
A: A curated Maven/Gradle dependency that pulls in all the libraries needed for a feature (e.g., spring-boot-starter-web pulls Spring MVC + Jackson + embedded Tomcat). You don't manage transitive dependencies manually.

**Q: How does Spring Boot know which port to run on?**
A: Default is 8080. Overridden by server.port in application.yml, environment variable SERVER_PORT, or --server.port command-line arg. In SkillSync, each service has a different port in its config-server yml.

**Q: What is Actuator? Which endpoints do you use?**
A: Actuator exposes management endpoints. In SkillSync we use /actuator/health for Kubernetes probes (readiness/liveness) and /actuator/prometheus for Prometheus metrics scraping.

**Q: What is the difference between application.yml and bootstrap.yml?**
A: bootstrap.yml is loaded before application.yml and is used for Spring Cloud Config — it contains the config-server URL so the app knows where to fetch its main config. In SkillSync, services use spring.config.import in application.yml (Spring Boot 2.4+ approach which replaces bootstrap.yml).

**Q: How do you run code at startup in Spring Boot?**
A: Implement CommandLineRunner or ApplicationRunner and mark it @Component. The run() method executes after the context is fully loaded.
