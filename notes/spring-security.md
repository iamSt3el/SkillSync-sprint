# Spring Security

## What is Spring Security?

Spring Security is the standard security framework for Spring applications. It handles:
- **Authentication** — who are you? (login, JWT validation)
- **Authorization** — what are you allowed to do? (role checks)
- **Protection** — CSRF, session fixation, clickjacking

In SkillSync, Spring Security is used in the **API Gateway** for JWT validation and in individual services for role-based access control.

---

## How Spring Security Works (Filter Chain)

Spring Security is implemented as a **chain of servlet filters** that intercept every HTTP request before it reaches your controller:

```
HTTP Request
    ↓
SecurityFilterChain
    ├── UsernamePasswordAuthenticationFilter (form login)
    ├── BearerTokenAuthenticationFilter (JWT)
    ├── BasicAuthenticationFilter (HTTP Basic)
    ├── ExceptionTranslationFilter (converts security exceptions to 401/403)
    ├── AuthorizationFilter (checks permissions)
    ↓
DispatcherServlet → Controller
```

Each filter either passes the request along or rejects it (returns 401/403).

---

## SecurityFilterChain Configuration (Modern Style)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // disable for REST APIs (stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register", "/auth/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/mentors/**").hasAnyRole("USER", "MENTOR", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## JWT Authentication Flow in SkillSync

```
1. User POSTs /auth/login with credentials
2. auth-service validates credentials → generates JWT
3. Client stores JWT and sends it in every request:
   Authorization: Bearer <token>
4. API Gateway intercepts the request
5. JwtAuthFilter extracts and validates the token
6. Sets Authentication in SecurityContextHolder
7. Request proceeds to the target service
```

### JWT Filter Implementation

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.isTokenValid(token)) {
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        username, null, jwtUtil.extractAuthorities(token));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## JWT Structure

A JWT has three Base64-encoded parts separated by dots:

```
header.payload.signature

eyJhbGciOiJIUzI1NiJ9          ← header (algorithm)
.eyJzdWIiOiJ1c2VyMSIsInJvbGVzIjpbIlVTRVIiXX0  ← payload (claims)
.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← signature
```

**Payload (claims):**
```json
{
  "sub": "user@example.com",
  "roles": ["ROLE_USER"],
  "iat": 1713100000,
  "exp": 1713186400
}
```

**Signature** = HMAC-SHA256(header + "." + payload, secret)

If the signature doesn't match, the token has been tampered with.

---

## UserDetails and UserDetailsService

Spring Security uses these to load user info:

```java
// Implement this to tell Spring how to load a user
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPasswordHash())
            .roles(user.getRole().name())
            .build();
    }
}
```

---

## PasswordEncoder

Never store plain-text passwords. Always hash:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // uses bcrypt — adaptive, salted
}

// Registering a user
String hashed = passwordEncoder.encode(rawPassword);
user.setPasswordHash(hashed);

// Verifying
boolean matches = passwordEncoder.matches(rawPassword, user.getPasswordHash());
```

BCrypt automatically generates a random salt and includes it in the hash — two identical passwords produce different hashes.

---

## Authorization — Role-Based Access Control

**Method-level security:**
```java
@EnableMethodSecurity  // enable in config class
```

```java
@GetMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public List<UserDTO> getAllUsers() { ... }

@PatchMapping("/mentors/{id}/approve")
@PreAuthorize("hasRole('ADMIN')")
public void approveMentor(@PathVariable Long id) { ... }

@GetMapping("/sessions/my")
@PreAuthorize("hasAnyRole('USER', 'MENTOR')")
public List<SessionDTO> getMySessions() { ... }

// Access the current user's data
@GetMapping("/profile")
@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
public UserDTO getProfile(@PathVariable Long userId) { ... }
```

**URL-level (in SecurityFilterChain):**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/skills").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET, "/skills").permitAll()
    .anyRequest().authenticated()
)
```

---

## SecurityContextHolder

Stores the current authenticated user's info during a request:

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
```

This is thread-local — each request has its own copy.

---

## CSRF Protection

CSRF (Cross-Site Request Forgery) attacks trick a browser into making requests to your server using the user's cookies.

- **REST APIs using JWT don't need CSRF protection** — JWTs are stored in localStorage/memory, not cookies, so CSRF can't exploit them.
- **Disable CSRF for REST APIs:** `csrf(csrf -> csrf.disable())`
- **Enable CSRF for session-based web apps** (Spring handles it automatically)

---

## CORS in Spring Security

CORS must be configured in Spring Security (not just @CrossOrigin) because security filters run before controllers:

```java
http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200", "https://skillsync.mooo.com"));
    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

---

## OAuth2 (Google Login)

SkillSync supports Google OAuth. Spring Security handles the flow:

```java
http.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> userInfo
        .userService(customOAuth2UserService))
    .successHandler(oAuth2SuccessHandler)
);
```

**OAuth2 flow:**
1. User clicks "Login with Google"
2. Browser redirected to Google consent screen
3. Google redirects back with auth code
4. Spring exchanges code for access token + user info
5. Your `OAuth2UserService` extracts user details, creates/finds user in DB
6. `successHandler` generates a JWT and redirects to frontend

---

## Authentication vs Authorization — Key Difference

| | Authentication | Authorization |
|---|---|---|
| Question | Who are you? | What can you do? |
| When | Before authorization | After authentication |
| Spring component | `AuthenticationManager` | `AuthorizationFilter` |
| Failure response | 401 Unauthorized | 403 Forbidden |

---

## Common Interview Questions

**Q: What is the difference between authentication and authorization?**
A: Authentication verifies identity (who are you — login, JWT validation). Authorization checks permissions (what can you do — role checks). Authentication happens first; authorization follows.

**Q: How does JWT work?**
A: A JWT has three parts: header (algorithm), payload (claims — user ID, roles, expiry), and signature. The server signs it with a secret key. On each request, the server verifies the signature — if valid, it trusts the claims without hitting the DB. Stateless authentication.

**Q: Why disable CSRF for REST APIs?**
A: CSRF attacks exploit browser cookie auto-sending. JWTs aren't stored in cookies — they're in Authorization headers or localStorage — so the browser won't automatically send them on cross-site requests. No cookie = no CSRF risk.

**Q: What is OncePerRequestFilter?**
A: A Spring Security filter that guarantees it runs exactly once per request (not twice in forwarded requests). Used for JWT validation — you extend it and implement doFilterInternal().

**Q: What is SecurityContextHolder?**
A: A thread-local store that holds the current request's Authentication object (who is logged in, what roles they have). Set by your JWT filter after validating the token. Cleared after the request completes.

**Q: What is BCrypt and why use it?**
A: BCrypt is an adaptive password hashing algorithm. It's adaptive (cost factor can increase as hardware gets faster), salted (random salt included, prevents rainbow table attacks), and slow by design (makes brute force expensive).

**Q: What is @PreAuthorize?**
A: A method-level security annotation that evaluates a SpEL expression before the method runs. Requires @EnableMethodSecurity. More flexible than URL patterns — can reference method arguments.

**Q: Difference between hasRole and hasAuthority?**
A: hasRole('ADMIN') automatically prepends "ROLE_" — checks for "ROLE_ADMIN". hasAuthority('ADMIN') checks the exact string "ADMIN". When you grant roles with .roles("ADMIN"), Spring stores them as "ROLE_ADMIN". Use hasRole() to match.
