# Spring AOP

## What is AOP?

AOP (Aspect-Oriented Programming) is a programming paradigm that addresses **cross-cutting concerns** — logic that spans multiple classes and can't be cleanly modularized with OOP alone.

**Examples of cross-cutting concerns:**
- Logging every method call
- Measuring execution time
- Security checks
- Transaction management
- Caching
- Audit trails

Without AOP, this logic is scattered (duplicated) across every class. AOP lets you write it **once** and apply it declaratively.

---

## Core Concepts

### Aspect
The class that contains the cross-cutting logic.
```java
@Aspect
@Component
public class LoggingAspect { ... }
```

### Join Point
A specific point in program execution where an aspect can be applied — in Spring AOP, this is always a **method execution**.

### Pointcut
An expression that selects which join points (methods) the aspect applies to.
```java
// All methods in any class in com.skillsync.mentorservice.service package
@Pointcut("execution(* com.skillsync.mentorservice.service.*.*(..))")
public void serviceLayer() {}
```

### Advice
The action taken at a join point. The *when* and *what* of an aspect.

| Advice | Annotation | When |
|---|---|---|
| Before | `@Before` | Before the method runs |
| After Returning | `@AfterReturning` | After method returns successfully |
| After Throwing | `@AfterThrowing` | After method throws an exception |
| After (Finally) | `@After` | After method (always — pass or fail) |
| Around | `@Around` | Wraps the method — you control execution |

### Weaving
The process of applying aspects to target objects. Spring uses **runtime weaving** via proxies (no bytecode modification).

---

## How Spring AOP Works (Proxy-Based)

Spring AOP uses **dynamic proxies**:

```
Your Code calls mentorService.approveMentor(id)
    ↓
Spring intercepts via Proxy
    ↓
@Before advice runs (logging)
    ↓
Actual mentorService.approveMentor(id) runs
    ↓
@AfterReturning advice runs (log success)
    ↓
Result returned to your code
```

Spring creates a proxy around each Spring bean. The proxy intercepts calls and delegates to advice before/after calling the real method.

**Two proxy types:**
- **JDK Dynamic Proxy** — used when the bean implements an interface
- **CGLIB Proxy** — used when the bean doesn't implement an interface (subclassing)

---

## Pointcut Expressions

```java
// All methods in a specific class
execution(* com.skillsync.mentorservice.service.MentorServiceImpl.*(..))

// All methods in any class in a package
execution(* com.skillsync.mentorservice.service.*.*(..))

// All methods in a package and sub-packages
execution(* com.skillsync..service.*.*(..))

// Methods with a specific return type
execution(MentorDTO com.skillsync.mentorservice.service.*.*(..))

// Methods with specific argument types
execution(* com.skillsync..*.*(Long, ..))

// Methods annotated with a specific annotation
@annotation(org.springframework.transaction.annotation.Transactional)

// All beans with a specific annotation
@within(org.springframework.stereotype.Service)
```

---

## @Before Advice

Runs before the method. Cannot stop execution (unless it throws an exception).

```java
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Before("execution(* com.skillsync.mentorservice.service.*.*(..))")
    public void logMethodEntry(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.debug("Entering: {} with args: {}", method, Arrays.toString(args));
    }
}
```

`JoinPoint` gives you access to: method name, arguments, target object, proxy object.

---

## @AfterReturning Advice

Runs after a method returns successfully. Can access the return value.

```java
@AfterReturning(
    pointcut = "execution(* com.skillsync.mentorservice.service.*.*(..))",
    returning = "result"
)
public void logMethodExit(JoinPoint joinPoint, Object result) {
    log.debug("Exiting: {} with result: {}", joinPoint.getSignature().toShortString(), result);
}
```

---

## @AfterThrowing Advice

Runs when a method throws an exception.

```java
@AfterThrowing(
    pointcut = "execution(* com.skillsync..service.*.*(..))",
    throwing = "ex"
)
public void logException(JoinPoint joinPoint, Exception ex) {
    log.error("Exception in {}: {}", joinPoint.getSignature().toShortString(), ex.getMessage());
}
```

---

## @Around Advice (Most Powerful)

Wraps the method completely. You control when (and whether) the actual method runs.

```java
@Around("execution(* com.skillsync..service.*.*(..))")
public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    String method = joinPoint.getSignature().toShortString();

    try {
        Object result = joinPoint.proceed();  // calls the actual method
        long duration = System.currentTimeMillis() - start;
        log.info("{} completed in {}ms", method, duration);
        return result;
    } catch (Throwable ex) {
        long duration = System.currentTimeMillis() - start;
        log.error("{} failed after {}ms: {}", method, duration, ex.getMessage());
        throw ex;
    }
}
```

`ProceedingJoinPoint.proceed()` calls the actual method. If you don't call it, the method never executes — useful for caching (return cached result without calling method).

---

## Named Pointcuts (Reusable)

```java
@Aspect
@Component
public class AppAspects {

    @Pointcut("execution(* com.skillsync..service.*.*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.skillsync..controller.*.*(..))")
    public void controllerLayer() {}

    @Pointcut("serviceLayer() || controllerLayer()")
    public void applicationLayer() {}

    @Before("serviceLayer()")
    public void logServiceCall(JoinPoint jp) { ... }

    @Around("applicationLayer()")
    public Object measureTime(ProceedingJoinPoint pjp) throws Throwable { ... }
}
```

---

## Custom Annotation-Based AOP

Create your own annotation and apply AOP to methods using it:

```java
// Define the annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String action();
}

// Use it
@Audited(action = "APPROVE_MENTOR")
public void approveMentor(Long mentorId) { ... }

// Intercept it with AOP
@Around("@annotation(audited)")
public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    log.info("User {} performing action: {}", user, audited.action());
    Object result = pjp.proceed();
    auditRepository.save(new AuditLog(user, audited.action(), LocalDateTime.now()));
    return result;
}
```

---

## Spring AOP Limitations

- **Only works on Spring beans** — AOP can't intercept calls on plain Java objects
- **Only intercepts external calls** — if a method within a bean calls another method in the same bean, the proxy is bypassed (self-invocation problem)
- **Method-level only** — can't intercept field access
- **Not for all cases** — AspectJ (compile-time weaving) is needed for more advanced scenarios

### Self-Invocation Problem

```java
@Service
public class MentorService {

    @Transactional
    public void outerMethod() {
        innerMethod();  // AOP (and @Transactional) WON'T apply to this call!
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void innerMethod() { ... }
}
```

Fix: inject the bean into itself (Spring will inject the proxy) or restructure code.

---

## AOP vs Interceptors vs Filters

| | Filter | Interceptor | AOP |
|---|---|---|---|
| Layer | Servlet (HTTP) | Spring MVC (HTTP) | Spring bean (any method) |
| Applies to | HTTP requests | HTTP requests | Any Spring bean method |
| Can intercept | Before servlet | Before/after controller | Any method call |
| Use for | Auth, logging HTTP | Request logging, auth | Business logic concerns |

---

## How @Transactional Uses AOP

`@Transactional` is implemented via AOP internally. When you call a transactional method:

1. Spring's `TransactionInterceptor` (an `@Around` advice) intercepts the call
2. Begins a transaction
3. Calls your actual method (`proceed()`)
4. Commits on success / rolls back on exception

You don't write any transaction management code — AOP handles it transparently.

Same for `@Cacheable`, `@Async`, `@Retryable`.

---

## SkillSync Context

In SkillSync, AOP is NOT explicitly used, but it works behind the scenes:
- `@Transactional` on service methods — Spring AOP handles transaction management
- `@Cacheable` in mentor-service/skill-service — Spring AOP intercepts and caches
- Manual logging with `@Slf4j` is used instead of a logging aspect

A logging aspect would centralise the per-method logs currently scattered across services.

---

## Common Interview Questions

**Q: What is AOP and why do we use it?**
A: AOP separates cross-cutting concerns (logging, security, transactions) from business logic. Without it, the same logging/security code is duplicated across every class. AOP lets you write it once as an Aspect and apply it declaratively via pointcut expressions.

**Q: What is the difference between @Before, @After, and @Around?**
A: @Before runs before the method. @After runs after (always, pass or fail). @Around wraps the method — you call proceed() to invoke the actual method and can execute code before and after. @Around is the most powerful and subsumes the others.

**Q: What is a Pointcut?**
A: An expression that selects which methods an aspect applies to. Spring uses AspectJ pointcut expression language — you can match by package, class, method name, return type, arguments, or annotation.

**Q: What is the self-invocation problem in Spring AOP?**
A: AOP works via proxies. If a method inside a bean calls another method in the same bean, it bypasses the proxy — so AOP advice (and @Transactional, @Cacheable) won't apply. Fix by injecting the bean via Spring so you get the proxy, not `this`.

**Q: How does @Transactional use AOP?**
A: Spring wraps @Transactional beans in a proxy with a TransactionInterceptor (an @Around advice) that begins a transaction, calls your method via proceed(), then commits or rolls back based on the outcome.

**Q: What is the difference between Spring AOP and AspectJ?**
A: Spring AOP uses runtime proxy-based weaving — it only works on Spring beans and only at method boundaries. AspectJ uses compile-time or load-time bytecode weaving — it can intercept field access, constructors, and non-Spring objects. Spring AOP is simpler; AspectJ is more powerful.

**Q: Can AOP intercept private methods?**
A: No. Spring AOP only intercepts public methods on Spring beans via proxies. Private methods can't be overridden by a proxy, so they can't be intercepted. AspectJ can intercept private methods via bytecode weaving.

**Q: Do you use AOP in SkillSync?**
A: Not explicitly, but implicitly — @Transactional and @Cacheable are both implemented via Spring AOP proxies. We use manual @Slf4j logging instead of a logging aspect for simplicity. A logging aspect would be a good improvement to centralise logging across all services.
