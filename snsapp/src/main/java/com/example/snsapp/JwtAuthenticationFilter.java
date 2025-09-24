package com.example.snsapp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Authorizationヘッダーからトークンを抽出
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                logger.debug("Authorization header found, extracted username='{}'", username);
            } catch (Exception e) {
                logger.warn("JWT token parsing failed: {}", e.getMessage());
            }
        } else {
            logger.trace("No Authorization header present on request {} {}", request.getMethod(), request.getRequestURI());
        }

        // トークンが有効で、まだ認証されていない場合
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException ex) {
                logger.warn("User not found by UserDetailsService: {}", username);
            } catch (Exception ex) {
                logger.warn("Error loading user by username: {}", ex.getMessage());
            }

            if (userDetails != null) {
                boolean valid = false;
                try {
                    valid = jwtUtil.validateToken(jwt);
                } catch (Exception ex) {
                    logger.warn("Error validating JWT token for user {}: {}", username, ex.getMessage());
                }
                if (valid) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("User '{}' authenticated via JWT", username);
                } else {
                    logger.debug("JWT validation failed for user '{}'", username);
                }
            } else {
                logger.debug("Skipping authentication: userDetails is null for username='{}'", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}