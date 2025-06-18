package com.dcarrillo.ecomerce.cartservice.security;


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
        Long userId = null;
        List<String> roles = new ArrayList<>();

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            jwt= authorizationHeader.substring(7);
            try {
                username = jwtUtils.extractUsername(jwt);
                Claims claims = jwtUtils.extractAllClaims(jwt);
                if (claims != null) {
                    roles = claims.get("roles", List.class);
                    userId = claims.get("userId", Long.class);
                }
            }catch (io.jsonwebtoken.ExpiredJwtException e){
                System.out.println("JWT token expiro" + e.getMessage());
            }catch (io.jsonwebtoken.JwtException e){
                System.out.println("Error parsing JWT Token" + e.getMessage());
            }
        }

        if (username != null && userId !=null && SecurityContextHolder.getContext().getAuthentication() == null){

            if (jwt !=null && !jwtUtils.isTokenExpired(jwt)){

                List<GrantedAuthority>authorities= new ArrayList<>();
                if (roles != null){
                    authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }
                UserPrincipal userPrincipal = new UserPrincipal(userId, username, "", authorities);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null , authorities);

                usernamePasswordAuthenticationToken
                        .setDetails( new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }else {
                System.out.println("Token JWT para " + username + " es invalido o a expirado");
            }

        }

        filterChain.doFilter(request, response);
    }
}