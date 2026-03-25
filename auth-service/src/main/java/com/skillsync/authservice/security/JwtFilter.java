package com.skillsync.authservice.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
	private final JwtUtil jwtUtil;

	public JwtFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	// get authorization header
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getServletPath();

	    // Skip auth endpoints - login and register
	    if (path.startsWith("/auth/login") || path.startsWith("/auth/register")) {
	        filterChain.doFilter(request, response);
	        return;
	    }
		String authHeader = request.getHeader("Authorization");
		String token = null;
		String email = null;

		// check bearer token
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
			try {
				email = jwtUtil.extractEmail(token);
			}
			catch(Exception e)
			{
				log.warn("JWT validation failed: {}", e.getMessage());
			}
		}
		//if email exists and user not already authenticated
		if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null)
		{
			//validate token
			if(jwtUtil.isTokenValid(token))
			{
				//extract roles
				List<String> roles = jwtUtil.extractRoles(token);
				//convert roles -> authorities
				List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
				//create authentication object
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email,  null, authorities);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				//set authentication in context
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		
		//continue filter chain
		filterChain.doFilter(request, response);
	}

}
