package com.skillsync.apiGateway.filter;

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

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Always strip incoming X-User-* headers to prevent client injection.
        // These headers are only set by the gateway after JWT validation.
        HttpServletRequestWrapper stripped = new HttpServletRequestWrapper(request) {
            private static final Set<String> INTERNAL_HEADERS =
                    Set.of("X-User-Email", "X-User-Role", "X-User-Id");

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

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || path.contains("/v3/api-docs")
                || path.contains("/swagger-ui")
                || path.contains("/webjars")) {
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

            // Build on top of `stripped` so injected headers are already removed
            HttpServletRequestWrapper mutatedRequest = new HttpServletRequestWrapper(stripped) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Email".equals(name)) return email;
                    if ("X-User-Role".equals(name)) return role;
                    if ("X-User-Id".equals(name)) return userId;
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Email".equals(name)) return Collections.enumeration(List.of(email));
                    if ("X-User-Role".equals(name)) return Collections.enumeration(List.of(role));
                    if ("X-User-Id".equals(name)) return Collections.enumeration(List.of(userId));
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    Set<String> names = new HashSet<>(Collections.list(super.getHeaderNames()));
                    names.add("X-User-Email");
                    names.add("X-User-Role");
                    names.add("X-User-Id");
                    return Collections.enumeration(names);
                }
            };

            filterChain.doFilter(mutatedRequest, response);

        } catch (JwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
