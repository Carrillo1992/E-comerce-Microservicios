package com.dcarrillo.ecomerce.productservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class JwtRequestFilter  extends OncePerRequestFilter {

    private final  JwtUtils jwtUtils;

    @Autowired
    public JwtRequestFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        List<String> rolesForToken = new ArrayList<>();

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            jwt= authorizationHeader.substring(7);
            try {
                username = jwtUtils.extractUsername(jwt);
                Claims claims = jwtUtils.extractAllClaims(jwt);
                rolesForToken = claims.get("roles", List.class);
            }catch (io.jsonwebtoken.ExpiredJwtException e){
                System.out.println("JWT token expiro" + e.getMessage());
            }catch (io.jsonwebtoken.JwtException e){
                System.out.println("Error parsing JWT Token" + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            List<GrantedAuthority>authorities= new ArrayList<>();
            if (rolesForToken != null){
                authorities = rolesForToken.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null , authorities);

            usernamePasswordAuthenticationToken
                    .setDetails( new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        }

        filterChain.doFilter(request, response);
    }
}