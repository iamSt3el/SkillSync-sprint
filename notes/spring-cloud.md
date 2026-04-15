# Spring Cloud

## What is Spring Cloud?

Spring Cloud provides tools for building distributed systems — microservices that need to talk to each other, discover each other, configure themselves, and handle failures gracefully.

**In SkillSync, you use:**
- Spring Cloud Netflix Eureka (service discovery)
- Spring Cloud Config (centralized configuration)
- Spring Cloud Gateway (API gateway)
- Spring Cloud OpenFeign (declarative HTTP clients)
- Resilience4j (circuit breaker — integrated with Spring Cloud)

---

## 1. Service Discovery — Eureka

### The Problem
In a microservice system, services need to call each other. But their IPs/ports change (containers restart, scale up/down). You can't hardcode URLs.

### The Solution — Service Registry
Each service **registers** itself with Eureka on startup. When service A wants to call service B, it asks Eureka for B's address.

```
session-service starts → registers with Eureka ("I'm session-service at 10.0.0.5:8085")
session-service wants to call mentor-service → asks Eureka → gets "10.0.0.3:8083"
```

### Eureka Server Setup
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication { ... }
```

```yaml
# eureka server application.yml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false  # server doesn't register itself
    fetch-registry: false
```

### Eureka Client (every microservice)
```java
@SpringBootApplication
@EnableDiscoveryClient  // or just have the dependency — Spring Boot auto-configures it
public class MentorServiceApplication { ... }
```

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

### How Feign Uses Eureka
```java
@FeignClient(name = "mentor-service")  // "mentor-service" is the registered name in Eureka
public interface MentorClient {
    @GetMapping("/mentors/{id}/exists")
    Boolean mentorExists(@PathVariable Long id);
}
```

Feign automatically asks Eureka for the address of "mentor-service" and load-balances across instances.

---

## 2. Centralized Configuration — Spring Cloud Config

### The Problem
12 services, each with its own `application.yml`. Changing a common property (RabbitMQ host, JWT secret) means updating 12 files and redeploying 12 services.

### The Solution — Config Server
One central server stores all configuration. Each service fetches its config on startup.

```
Config Server → reads from Git repo (skillsync-config)
Microservices → fetch config from Config Server on startup
```

### Config Server Setup
```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication { ... }
```

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/iamSt3el/skillsync-config.git
          default-label: d-configs
```

### Config Files in Git Repo
Filename convention: `{application-name}.yml` or `{application-name}-{profile}.yml`

```
skillsync-config/
├── application.yml          ← shared by ALL services
├── mentor-service.yml       ← mentor-service specific
├── session-service.yml      ← session-service specific
└── auth-service.yml         ← auth-service specific
```

### Client Setup
```yaml
# Each service's application.yml
spring:
  application:
    name: mentor-service  # must match the config file name
  config:
    import: "optional:configserver:http://config-server:8888"
```

### Refresh Without Restart
```java
@RefreshScope  // re-injects @Value fields when config changes
@RestController
public class SomeController {
    @Value("${some.property}")
    private String value;
}
```

Trigger refresh: `POST /actuator/refresh`

---

## 3. API Gateway — Spring Cloud Gateway

### The Problem
Clients shouldn't know about 10 different service URLs. Security (JWT) shouldn't be duplicated in every service.

### The Solution — API Gateway
Single entry point for all clients. Handles routing, authentication, rate limiting, CORS.

```java
@SpringBootApplication
public class ApiGatewayApplication { ... }
// No special annotation needed — configured via application.yml
```

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: mentor-service
          uri: lb://mentor-service        # lb:// = load-balanced via Eureka
          predicates:
            - Path=/mentors/**
          filters:
            - StripPrefix=0

        - id: session-service
          uri: lb://session-service
          predicates:
            - Path=/sessions/**
```

### Gateway Filter — JWT Validation
```java
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());

        if (token == null || !jwtUtil.isValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);  // pass to next filter / route to service
    }
}
```

**Key difference:** Spring Cloud Gateway is **reactive** (built on WebFlux/Project Reactor), not servlet-based.

---

## 4. Feign Client — Declarative HTTP

### Without Feign (ugly)
```java
RestTemplate restTemplate = new RestTemplate();
Boolean exists = restTemplate.getForObject(
    "http://mentor-service/mentors/" + mentorId + "/exists", Boolean.class);
```

### With Feign (clean)
```java
@FeignClient(name = "mentor-service", fallback = MentorClientFallback.class)
public interface MentorClient {
    @GetMapping("/mentors/{id}/exists")
    Boolean mentorExists(@PathVariable("id") Long mentorId);
}

// Inject and use like a normal service
@Autowired
private MentorClient mentorClient;

boolean exists = mentorClient.mentorExists(mentorId);
```

Feign handles:
- Eureka lookup (resolves `mentor-service` to actual IP)
- Load balancing (Spring Cloud LoadBalancer)
- Serialization/Deserialization (Jackson)
- Circuit breaker integration (Resilience4j)

---

## 5. Circuit Breaker — Resilience4j

### Circuit Breaker States
```
CLOSED → (failures exceed threshold) → OPEN → (wait duration) → HALF-OPEN → (success) → CLOSED
                                                                             → (failure) → OPEN
```

### Configuration
```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true

resilience4j:
  circuitbreaker:
    instances:
      mentor-service:
        sliding-window-size: 5          # evaluate last 5 calls
        failure-rate-threshold: 50      # trip at 50% failure rate
        wait-duration-in-open-state: 10s # stay open for 10s before trying again
```

### Fallback
```java
@Component
public class MentorClientFallback implements MentorClient {
    @Override
    public Boolean mentorExists(Long mentorId) {
        log.warn("mentor-service unavailable for mentorId={}", mentorId);
        throw new ServiceUnavailableException("Mentor Service Unavailable");
    }
}
```

---

## 6. Load Balancing — Spring Cloud LoadBalancer

When Feign resolves a service name, Spring Cloud LoadBalancer distributes calls across multiple instances using **Round Robin** by default.

```
mentor-service instance 1: 10.0.0.1:8083
mentor-service instance 2: 10.0.0.2:8083

Request 1 → 10.0.0.1
Request 2 → 10.0.0.2
Request 3 → 10.0.0.1
...
```

This is transparent — you just use the service name, load balancing happens automatically.

---

## Startup Order in SkillSync

```
MySQL + RabbitMQ + Redis   (infra — must be ready)
        ↓
  config-server             (must be ready — services fetch config from here)
        ↓
  eureka-server             (must be ready — services register here)
        ↓
  all business services     (parallel — register with Eureka, fetch config)
        ↓
  api-gateway               (last — needs services registered to route to them)
```

This order is enforced in your Jenkinsfile (stages 7 → 8 → 9 → 10 → 11).

---

## Common Interview Questions

**Q: What is service discovery and why do we need it?**
A: In a microservice system, service instances are dynamic — IPs change when containers restart or scale. Service discovery (Eureka) lets services register themselves and be found by name rather than IP. This enables location-transparent, load-balanced communication.

**Q: How does Eureka work?**
A: Services register on startup (heartbeat every 30s). Eureka maintains a registry. Clients fetch the registry and cache it locally. When a service goes down and stops sending heartbeats, Eureka removes it after ~90s. Clients use the cached registry to make calls.

**Q: What is the API Gateway pattern?**
A: A single entry point for all client requests. It handles cross-cutting concerns like JWT authentication, routing, rate limiting, and CORS — so each microservice doesn't need to implement these independently.

**Q: Why use Feign over RestTemplate?**
A: Feign is declarative — you define an interface with annotations and Spring generates the implementation. It integrates natively with Eureka (resolves service names) and Resilience4j (circuit breaker). RestTemplate requires manual URL building and error handling.

**Q: What happens when config-server is down at startup?**
A: With `optional:configserver:...`, the service starts using its local application.yml as fallback. Without `optional:`, it throws an exception and fails to start.

**Q: What is the difference between lb:// and http:// in Gateway routes?**
A: lb:// tells the gateway to use Spring Cloud LoadBalancer to resolve the service name via Eureka and load balance across instances. http:// is a direct URL with no discovery.

**Q: How does Spring Cloud Config differ from Kubernetes ConfigMap?**
A: Both externalize config. Spring Cloud Config stores in Git (versioned, environment-specific files, supports @RefreshScope for dynamic refresh). Kubernetes ConfigMap is cluster-managed, injected as env vars or files. In SkillSync, you use both — ConfigMap for shared k8s env vars and Config Server for Spring-specific properties.

**Q: What is @RefreshScope?**
A: Marks a bean to be re-created when a config refresh is triggered (POST /actuator/refresh). Used with @Value fields that need to pick up updated properties from Config Server without restarting the service.
