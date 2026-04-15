# Spring REST

## What is REST?

REST (Representational State Transfer) is an architectural style for building web APIs. It defines a set of constraints:

1. **Client-Server** — UI and backend are separate
2. **Stateless** — every request contains all the info needed; server stores no session
3. **Cacheable** — responses can be cached
4. **Uniform Interface** — consistent URLs, HTTP methods, status codes
5. **Layered System** — client doesn't know if it's talking to a gateway, load balancer, or actual server

A web API that follows REST constraints is called a **RESTful API**.

---

## HTTP Methods and Their Meaning

| Method | Purpose | Idempotent | Safe |
|---|---|---|---|
| GET | Retrieve resource | Yes | Yes |
| POST | Create resource | No | No |
| PUT | Replace resource entirely | Yes | No |
| PATCH | Partially update resource | No | No |
| DELETE | Delete resource | Yes | No |

- **Idempotent** — calling it multiple times has the same effect as calling it once
- **Safe** — calling it does not change server state

---

## REST URL Design Conventions

```
GET    /mentors           → list all mentors
POST   /mentors           → create a mentor
GET    /mentors/{id}      → get one mentor
PUT    /mentors/{id}      → replace mentor
PATCH  /mentors/{id}      → partial update
DELETE /mentors/{id}      → delete mentor

GET    /mentors/{id}/sessions   → sessions for a mentor (nested resource)
POST   /sessions/{id}/accept    → action on a resource (verb as last segment)
```

**Rules:**
- Use nouns, not verbs (`/mentors` not `/getMentors`)
- Use plural nouns (`/mentors` not `/mentor`)
- Lowercase, hyphen-separated (`/skill-categories`)
- Hierarchy for nested resources (`/mentors/{id}/reviews`)

---

## HTTP Status Codes

| Code | Meaning | When to use |
|---|---|---|
| 200 OK | Success | GET, PUT, PATCH responses |
| 201 Created | Resource created | POST that creates |
| 204 No Content | Success, no body | DELETE, PUT with no response |
| 400 Bad Request | Invalid input | Validation errors |
| 401 Unauthorized | Not authenticated | Missing/invalid JWT |
| 403 Forbidden | Not authorized | Valid JWT but wrong role |
| 404 Not Found | Resource doesn't exist | |
| 409 Conflict | State conflict | Duplicate application, booking conflict |
| 422 Unprocessable Entity | Semantic validation error | |
| 500 Internal Server Error | Unexpected server error | |
| 503 Service Unavailable | Service down | Circuit breaker open |

---

## Building REST APIs in Spring

```java
@RestController
@RequestMapping("/mentors")
public class MentorController {

    private final MentorService mentorService;

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    @GetMapping
    public ResponseEntity<List<MentorDTO>> getAllMentors() {
        return ResponseEntity.ok(mentorService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorDTO> getMentor(@PathVariable Long id) {
        return ResponseEntity.ok(mentorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<MentorDTO> applyAsMentor(@Valid @RequestBody ApplyMentorRequest req) {
        MentorDTO created = mentorService.apply(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveMentor(@PathVariable Long id) {
        mentorService.approve(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMentor(@PathVariable Long id) {
        mentorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## DTOs (Data Transfer Objects)

Never expose your entity directly. Use DTOs to:
- Control what fields are exposed
- Shape the response for the client
- Decouple API contract from DB schema

```java
// Entity (DB)
@Entity
public class Mentor {
    private Long id;
    private Long userId;
    private String bio;
    private String status;   // PENDING, APPROVED, REJECTED
    private double rating;
    private String passwordHash;  // should NOT be exposed!
}

// DTO (API response)
public class MentorDTO {
    private Long id;
    private String bio;
    private double rating;
    private List<String> skills;
    // passwordHash is absent — intentionally not exposed
}
```

---

## RestTemplate (old) vs WebClient (new) vs Feign

For making HTTP calls to other services:

**RestTemplate (synchronous, deprecated in Spring 6):**
```java
MentorDTO mentor = restTemplate.getForObject(
    "http://mentor-service/mentors/" + id, MentorDTO.class);
```

**WebClient (reactive, non-blocking):**
```java
MentorDTO mentor = webClient.get()
    .uri("/mentors/{id}", id)
    .retrieve()
    .bodyToMono(MentorDTO.class)
    .block();
```

**Feign Client (declarative — what SkillSync uses):**
```java
@FeignClient(name = "mentor-service", fallback = MentorClientFallback.class)
public interface MentorClient {
    @GetMapping("/mentors/{id}/exists")
    Boolean mentorExists(@PathVariable Long id);
}
```

Feign is the cleanest — you just define an interface, Spring generates the implementation.

---

## Pagination and Sorting

For list endpoints, always paginate large datasets:

```java
@GetMapping
public ResponseEntity<Page<MentorDTO>> getMentors(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "id") String sortBy
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    return ResponseEntity.ok(mentorService.getAll(pageable));
}
```

Response includes: `content`, `totalElements`, `totalPages`, `number`, `size`.

---

## API Versioning

When you change an API, existing clients break. Versioning strategies:

**URL versioning (most common):**
```
/api/v1/mentors
/api/v2/mentors
```

**Header versioning:**
```
Accept: application/vnd.skillsync.v2+json
```

**Query param versioning:**
```
/mentors?version=2
```

In SkillSync, your API gateway routes to `/api/*` — you can add versioning there.

---

## HATEOAS (Hypermedia)

Advanced REST concept — responses include links to related actions:
```json
{
  "id": 1,
  "name": "John",
  "_links": {
    "self": { "href": "/mentors/1" },
    "sessions": { "href": "/mentors/1/sessions" },
    "reviews": { "href": "/mentors/1/reviews" }
  }
}
```

Spring provides `spring-hateoas` for this. Not commonly required in sprint projects.

---

## Idempotency Key Pattern

For POST requests (non-idempotent by default), clients can send an `Idempotency-Key` header. The server stores the result and returns it if the same key is seen again — prevents duplicate bookings on retry.

In SkillSync, you use `eventId` in notification-service to prevent duplicate notifications — same concept.

---

## Common Interview Questions

**Q: What is REST? What are its constraints?**
A: REST is an architectural style with constraints: client-server, stateless, cacheable, uniform interface, layered system. Stateless is the most important — the server holds no session; every request is self-contained.

**Q: Difference between PUT and PATCH?**
A: PUT replaces the entire resource. PATCH applies a partial update. PUT is idempotent (same result every call); PATCH technically isn't but is often implemented that way.

**Q: Difference between 401 and 403?**
A: 401 Unauthorized — you're not authenticated (no token or invalid token). 403 Forbidden — you're authenticated but don't have permission (valid token, wrong role).

**Q: Why use DTOs instead of entities?**
A: Control what's exposed (don't leak password hashes, internal IDs), decouple the API contract from the DB schema, shape the response for different clients.

**Q: What is Feign and why do you use it over RestTemplate?**
A: Feign is a declarative HTTP client — you define an interface with annotations and Spring generates the implementation. RestTemplate requires manual URL building and response parsing. Feign integrates with Eureka (resolves service names) and Resilience4j (circuit breaker). Much less boilerplate.

**Q: What HTTP status code do you return when creating a resource?**
A: 201 Created, with the created resource in the body and optionally a Location header pointing to the new resource URL.

**Q: What is idempotency and why does it matter?**
A: An operation is idempotent if calling it multiple times produces the same result as calling it once. GET, PUT, DELETE are idempotent. POST is not. Matters for retries — a client can safely retry an idempotent request on network failure.

**Q: How do you handle API versioning?**
A: URL versioning (/api/v1/...) is most common and explicit. Header versioning is cleaner but harder to test in a browser. Query param versioning is simple but can interfere with caching.
