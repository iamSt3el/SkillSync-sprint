# Spring Core

## What is Spring Core?

Spring Core is the foundation of the entire Spring Framework. It provides the **IoC (Inversion of Control) container** and **Dependency Injection (DI)** mechanism. Everything else in Spring (MVC, Boot, Security, etc.) is built on top of Spring Core.

---

## IoC — Inversion of Control

Traditionally, your code creates its own dependencies:
```java
// Without IoC — you control object creation
UserService userService = new UserService(new UserRepository());
```

With IoC, the **container** creates and manages objects for you:
```java
// With IoC — Spring controls object creation
@Autowired
private UserService userService;
```

You invert the control of object creation from your code to the framework. That's IoC.

---

## Dependency Injection

DI is the mechanism Spring uses to implement IoC. Instead of a class creating its own dependencies, they are **injected** from outside.

### 3 Types of DI

**1. Constructor Injection (preferred)**
```java
@Service
public class SessionService {
    private final MentorClient mentorClient;

    public SessionService(MentorClient mentorClient) {
        this.mentorClient = mentorClient;
    }
}
```

**2. Setter Injection**
```java
@Autowired
public void setMentorClient(MentorClient mentorClient) {
    this.mentorClient = mentorClient;
}
```

**3. Field Injection (not recommended)**
```java
@Autowired
private MentorClient mentorClient;
```

**Why constructor injection is preferred:**
- Dependencies are explicit and required at object creation
- Makes the class easy to unit test (no Spring context needed)
- Fields can be `final` — guarantees immutability
- Easier to spot when a class has too many dependencies

---

## The Spring IoC Container

Two main container implementations:

| Container | Description |
|---|---|
| `BeanFactory` | Basic container, lazy initialization |
| `ApplicationContext` | Extended container, eager init, events, i18n. Use this. |

**ApplicationContext implementations:**
- `AnnotationConfigApplicationContext` — for Java config
- `ClassPathXmlApplicationContext` — for XML config (old)
- `SpringApplication` (Spring Boot) — auto-configured ApplicationContext

---

## Beans

A **Bean** is any object managed by the Spring container.

### Declaring Beans

**Via stereotype annotations:**
```java
@Component       // generic
@Service         // business logic layer
@Repository      // data access layer (also enables exception translation)
@Controller      // web layer
@RestController  // web layer + @ResponseBody
```

**Via @Bean in a @Configuration class:**
```java
@Configuration
public class AppConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Bean Scopes

| Scope | Description |
|---|---|
| `singleton` | One instance per container (default) |
| `prototype` | New instance every time it's requested |
| `request` | One per HTTP request (web only) |
| `session` | One per HTTP session (web only) |

```java
@Bean
@Scope("prototype")
public SomeService someService() { ... }
```

---

## Bean Lifecycle

```
Container starts
    → Instantiation (constructor called)
    → Dependency Injection (@Autowired fields/setters filled)
    → @PostConstruct method runs
    → Bean is ready for use
    ...
    → @PreDestroy method runs
    → Container shuts down
```

```java
@Component
public class CacheWarmer {
    @PostConstruct
    public void init() {
        // load cache on startup
    }

    @PreDestroy
    public void cleanup() {
        // flush cache on shutdown
    }
}
```

---

## @Autowired and Autowiring Rules

Spring resolves beans by **type** first, then **name** if multiple candidates exist.

**Ambiguity problem — two beans of the same type:**
```java
@Component("mysqlUserRepo")
public class MySQLUserRepository implements UserRepository {}

@Component("mongoUserRepo")
public class MongoUserRepository implements UserRepository {}
```

**Solutions:**

`@Qualifier` — specify which one:
```java
@Autowired
@Qualifier("mysqlUserRepo")
private UserRepository userRepository;
```

`@Primary` — mark one as the default:
```java
@Primary
@Component
public class MySQLUserRepository implements UserRepository {}
```

---

## @Component Scanning

Spring scans a package and its sub-packages for annotated classes:
```java
@SpringBootApplication  // includes @ComponentScan of the current package
public class AuthServiceApplication { ... }
```

You can customize:
```java
@ComponentScan(basePackages = "com.skillsync.authservice")
```

---

## @Configuration and @Bean

```java
@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(BCryptPasswordEncoder encoder) {
        // Spring injects the encoder bean above automatically
        return new ProviderManager(...);
    }
}
```

`@Configuration` classes use CGLIB proxying — calling `encoder()` multiple times returns the **same** singleton bean, not a new instance each time.

---

## @Value and @PropertySource

Inject values from properties files:
```java
@Value("${jwt.secret}")
private String jwtSecret;

@Value("${jwt.expiration:86400000}")  // with default
private long expiration;
```

---

## Spring Expression Language (SpEL)

```java
@Value("#{systemProperties['user.region']}")
private String region;
```

---

## Profiles

Run different beans in different environments:
```java
@Bean
@Profile("dev")
public DataSource devDataSource() { ... }

@Bean
@Profile("prod")
public DataSource prodDataSource() { ... }
```

Activate with: `spring.profiles.active=prod`

---

## Event System

Spring has a built-in event mechanism (different from RabbitMQ — this is in-process only):
```java
// Publish
applicationEventPublisher.publishEvent(new UserRegisteredEvent(this, userId));

// Listen
@EventListener
public void onUserRegistered(UserRegisteredEvent event) { ... }
```

---

## AOP in Spring Core

Spring AOP is built on top of Spring Core's proxy mechanism. When you annotate a bean with `@Transactional` or `@Cacheable`, Spring wraps it in a proxy that intercepts calls.

---

## Common Interview Questions

**Q: What is the difference between IoC and DI?**
A: IoC is the principle — give up control of object creation. DI is the implementation — inject dependencies from outside. DI is one way to achieve IoC.

**Q: What is the difference between BeanFactory and ApplicationContext?**
A: ApplicationContext extends BeanFactory and adds: eager singleton initialization, event publishing, i18n, AOP support. BeanFactory is lazy and minimal. Always use ApplicationContext.

**Q: What is the default bean scope?**
A: Singleton — one instance per Spring container.

**Q: Why is field injection bad?**
A: You can't make fields final, can't test without Spring context, and hides the number of dependencies making it easy to violate SRP.

**Q: What is @Primary vs @Qualifier?**
A: @Primary marks one bean as the default when multiple candidates exist. @Qualifier explicitly names which bean to inject at the injection point. @Qualifier takes precedence over @Primary.

**Q: What happens if you call a @Bean method twice inside a @Configuration class?**
A: Because @Configuration uses CGLIB proxying, the second call returns the existing singleton bean, not a new instance. This is NOT the case with @Component classes.

**Q: What is @PostConstruct used for?**
A: Runs after dependency injection is complete. Use it for initialization logic (e.g., loading cache, validating config). Better than constructor initialization because all dependencies are guaranteed to be injected.

**Q: Difference between @Component, @Service, @Repository?**
A: Functionally identical — all register beans. @Repository additionally enables Spring's PersistenceExceptionTranslation (converts DB-specific exceptions to Spring's DataAccessException). @Service and @Component are semantically different but technically the same.
