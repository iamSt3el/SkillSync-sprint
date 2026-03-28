package com.skillsync.apigateway.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/actuator"
    );

    static final String HEADER_USER_EMAIL = "X-User-Email";
    static final String HEADER_USER_ROLE  = "X-User-Role";
    static final String HEADER_USER_ID    = "X-User-Id";

    private static final Set<String> INTERNAL_HEADERS =
            Set.of(HEADER_USER_EMAIL, HEADER_USER_ROLE, HEADER_USER_ID);

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Always strip incoming X-User-* headers to prevent client injection.
        // These headers are only set by the gateway after JWT validation.
        HttpServletRequest stripped = stripInternalHeaders(request);

        // Allow CORS preflight requests through without JWT validation
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(stripped, response);
            return;
        }

        if (isPublicPath(path)) {
            filterChain.doFilter(stripped, response);
            return;
        }

        String authHeader = stripped.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            String role = (roles != null && !roles.isEmpty()) ? roles.get(0) : "";
            String userId = claims.get("userId") != null ? claims.get("userId").toString() : "";

            if (path.startsWith("/admin/") && !"ROLE_ADMIN".equals(role)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return;
            }

            filterChain.doFilter(injectUserHeaders(stripped, email, role, userId), response);

        } catch (JwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    private static boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || path.contains("/v3/api-docs")
                || path.contains("/swagger-ui")
                || path.contains("/webjars");
    }

    private static HttpServletRequestWrapper stripInternalHeaders(HttpServletRequest request) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (INTERNAL_HEADERS.contains(name)) return null;
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (INTERNAL_HEADERS.contains(name)) return Collections.emptyEnumeration();
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> names = new HashSet<>(Collections.list(super.getHeaderNames()));
                INTERNAL_HEADERS.forEach(names::remove);
                return Collections.enumeration(names);
            }
        };
    }

    private static HttpServletRequestWrapper injectUserHeaders(
            HttpServletRequest request, String email, String role, String userId) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (HEADER_USER_EMAIL.equals(name)) return email;
                if (HEADER_USER_ROLE.equals(name)) return role;
                if (HEADER_USER_ID.equals(name)) return userId;
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (HEADER_USER_EMAIL.equals(name)) return Collections.enumeration(List.of(email));
                if (HEADER_USER_ROLE.equals(name)) return Collections.enumeration(List.of(role));
                if (HEADER_USER_ID.equals(name)) return Collections.enumeration(List.of(userId));
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> names = new HashSet<>(Collections.list(super.getHeaderNames()));
                names.add(HEADER_USER_EMAIL);
                names.add(HEADER_USER_ROLE);
                names.add(HEADER_USER_ID);
                return Collections.enumeration(names);
            }
        };
    }
}
