# Spring MVC

## What is Spring MVC?

Spring MVC is the web framework within Spring. It follows the **Model-View-Controller** pattern to separate concerns in web applications.

- **Model** — data / business objects
- **View** — what the user sees (HTML, JSON, XML)
- **Controller** — handles HTTP requests, coordinates model and view

In modern REST APIs (like SkillSync), the "View" is just JSON — there's no HTML template rendering.

---

## How a Request Flows Through Spring MVC

```
HTTP Request
    ↓
DispatcherServlet  (Front Controller — single entry point)
    ↓
HandlerMapping     (which controller/method handles this URL?)
    ↓
HandlerAdapter     (invokes the controller method)
    ↓
Controller Method  (your @GetMapping / @PostMapping method)
    ↓
View Resolver      (for REST: converts return value to JSON via HttpMessageConverter)
    ↓
HTTP Response
```

**DispatcherServlet** is the heart of Spring MVC. All requests go through it first.

---

## Core Annotations

### @Controller vs @RestController

```java
@Controller                    // returns view name (for HTML templates)
public class PageController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("user", "John");
        return "home";  // resolves to home.html template
    }
}

@RestController                // @Controller + @ResponseBody on every method
public class MentorController {
    @GetMapping("/mentors")
    public List<MentorDTO> getMentors() {
        return mentors;  // automatically serialized to JSON
    }
}
```

### Request Mapping Annotations

```java
@GetMapping("/mentors")           // GET
@PostMapping("/mentors")          // POST
@PutMapping("/mentors/{id}")      // PUT
@PatchMapping("/mentors/{id}")    // PATCH
@DeleteMapping("/mentors/{id}")   // DELETE
@RequestMapping(value="/mentors", method=RequestMethod.GET)  // generic form
```

### Method Parameters

```java
@GetMapping("/mentors/{id}")
public MentorDTO getMentor(
    @PathVariable Long id,                    // from URL path
    @RequestParam(required=false) String skill, // from query string ?skill=Java
    @RequestHeader("Authorization") String token, // from header
    @RequestBody CreateMentorRequest request, // from request body (JSON)
    Principal principal                       // current authenticated user
) { ... }
```

### Response

```java
// Return just the body (200 OK)
@GetMapping("/mentors/{id}")
public MentorDTO getMentor(@PathVariable Long id) {
    return mentorService.getMentor(id);
}

// Control status code and headers
@PostMapping("/mentors")
public ResponseEntity<MentorDTO> createMentor(@RequestBody CreateMentorRequest req) {
    MentorDTO created = mentorService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// Return 204 No Content
@DeleteMapping("/mentors/{id}")
public ResponseEntity<Void> deleteMentor(@PathVariable Long id) {
    mentorService.delete(id);
    return ResponseEntity.noContent().build();
}
```

---

## @RequestBody and @ResponseBody

```java
@PostMapping("/sessions")
public ResponseEntity<SessionDTO> bookSession(
    @RequestBody BookSessionRequest request  // JSON body → Java object (deserialization)
) {
    SessionDTO result = sessionService.book(request);
    return ResponseEntity.ok(result);  // Java object → JSON (serialization)
}
```

Spring uses **Jackson** (`ObjectMapper`) to convert between JSON and Java objects automatically.

---

## Validation

```java
// DTO with validation annotations
public class BookSessionRequest {
    @NotNull
    private Long mentorId;

    @NotBlank
    @Size(min = 10, max = 500)
    private String topic;

    @Future
    private LocalDateTime scheduledAt;
}

// Controller — trigger validation with @Valid
@PostMapping("/sessions")
public ResponseEntity<SessionDTO> bookSession(@Valid @RequestBody BookSessionRequest req) {
    ...
}
```

If validation fails, Spring throws `MethodArgumentNotValidException` — handle it in `@ExceptionHandler`.

---

## Exception Handling

### @ExceptionHandler (local to one controller)
```java
@ExceptionHandler(MentorNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(MentorNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage()));
}
```

### @ControllerAdvice (global — handles exceptions across all controllers)
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MentorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(MentorNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
            .stream().map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse(500, "Internal server error"));
    }
}
```

---

## HttpMessageConverter

Spring MVC uses `HttpMessageConverter` to serialize/deserialize request and response bodies. Jackson's `MappingJackson2HttpMessageConverter` handles JSON automatically when Jackson is on the classpath (it is, via `spring-boot-starter-web`).

---

## Model, ModelAndView

For server-side rendering (Thymeleaf, JSP) — not relevant for REST APIs like SkillSync, but good to know:
```java
@GetMapping("/dashboard")
public ModelAndView dashboard() {
    ModelAndView mav = new ModelAndView("dashboard");
    mav.addObject("username", "John");
    return mav;
}
```

---

## Content Negotiation

Spring can return different formats based on the `Accept` header:
- `Accept: application/json` → Jackson serializes to JSON
- `Accept: application/xml` → JAXB serializes to XML (if configured)

---

## Filters vs Interceptors

| | Filter | Interceptor |
|---|---|---|
| Layer | Servlet level (before Spring) | Spring MVC level (after DispatcherServlet) |
| Access to Spring context | No | Yes |
| Used for | CORS, logging, auth token extraction | Logging, auth checks, pre/post processing |
| Interface | `javax.servlet.Filter` | `HandlerInterceptor` |

**In SkillSync:** JWT extraction happens in a filter (or OncePerRequestFilter in Spring Security). CORS is configured at the Spring Security level.

---

## CORS Configuration

```java
@RestController
@CrossOrigin(origins = "http://localhost:4200")  // allow Angular dev server
public class MentorController { ... }

// Or globally:
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200"));
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
    config.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

---

## Common Interview Questions

**Q: What is DispatcherServlet?**
A: The Front Controller in Spring MVC. All HTTP requests go through it first. It delegates to HandlerMapping to find the right controller, then to HandlerAdapter to invoke it, then to ViewResolver to render the response.

**Q: Difference between @Controller and @RestController?**
A: @RestController = @Controller + @ResponseBody on every method. @Controller returns view names for template rendering. @RestController returns data directly serialized to JSON/XML.

**Q: What is @RequestBody vs @RequestParam?**
A: @RequestBody reads the HTTP request body (JSON/XML) and deserializes it into a Java object. @RequestParam reads a query string parameter (?key=value) or form field. They serve completely different purposes.

**Q: How does Spring MVC convert Java objects to JSON?**
A: Via HttpMessageConverter. Jackson's MappingJackson2HttpMessageConverter is auto-configured when Jackson is on the classpath. It uses ObjectMapper to serialize/deserialize.

**Q: What is @ControllerAdvice?**
A: A global exception handler that applies @ExceptionHandler methods across all controllers. Without it, you'd have to duplicate exception handling in every controller.

**Q: What is ResponseEntity?**
A: A wrapper that lets you control the full HTTP response — status code, headers, and body. Return ResponseEntity<T> from a controller method when you need to set a specific status code (e.g., 201 CREATED, 204 NO CONTENT).

**Q: What is the difference between @PathVariable and @RequestParam?**
A: @PathVariable extracts values from the URL path (/mentors/{id} → id). @RequestParam extracts values from the query string (/mentors?skill=Java → skill).

**Q: How do you handle validation errors globally?**
A: Add @Valid to the @RequestBody parameter, then handle MethodArgumentNotValidException in a @RestControllerAdvice class to return a structured error response.
