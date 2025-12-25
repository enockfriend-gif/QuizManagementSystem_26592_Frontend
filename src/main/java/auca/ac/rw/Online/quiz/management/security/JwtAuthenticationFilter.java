package auca.ac.rw.Online.quiz.management.security;

import auca.ac.rw.Online.quiz.management.config.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request, 
                                    @org.springframework.lang.NonNull HttpServletResponse response, 
                                    @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String uri = request.getRequestURI();

        System.out.println("[JWT Filter] Filtering request: " + uri);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("[JWT Filter] No Bearer token found for: " + uri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            String username = jwtService.extractUsername(token);
            System.out.println("[JWT Filter] Found token for user: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                                null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        System.out.println(
                                "[JWT Filter] User authenticated: " + username + " with authorities: "
                                        + userDetails.getAuthorities());
                    } else {
                        System.out.println("[JWT Filter] Token invalid for user: " + username);
                    }
                } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                    System.err.println("[JWT Filter] User not found: " + username);
                }
            } else if (username == null) {
                System.err.println("[JWT Filter] Could not extract username from token");
            } else {
                System.out.println("[JWT Filter] User already authenticated: " + username);
            }
        } catch (Exception e) {
            System.err.println("[JWT Filter] ERROR validating token for " + uri + ": " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
