package com.dcarrillo.ecomerce.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements Ordered, WebFilter {

    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtils jwtUtils;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Rutas públicas: auth siempre, products solo para GET
    private final List<String> publicAuthPaths = List.of("/api/v1/auth/**");
    private final List<String> publicGetProductPaths = List.of(
            "/api/v1/products",
            "/api/v1/products/**" // Esto haría que /api/v1/products/{id} también sea público para GET
    );

    @Autowired
    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = String.valueOf(request.getMethod());

        logger.info("API Gateway Filter: Procesando {} {}", method, path); // Log inicial

        boolean isPublicPath = false;
        for (String publicPattern : publicAuthPaths) {
            if (pathMatcher.match(publicPattern, path)) {
                isPublicPath = true;
                break;
            }
        }
        if (!isPublicPath && "GET".equalsIgnoreCase(method)) {
            for (String publicGetPattern : publicGetProductPaths) {
                if (pathMatcher.match(publicGetPattern, path)) {
                    isPublicPath = true;
                    break;
                }
            }
        }

        if (isPublicPath) {
            logger.debug("Ruta pública: {} {}, permitiendo acceso sin validación de token.", method, path);
            return chain.filter(exchange);
        }

        logger.debug("Ruta protegida: {} {}, buscando token JWT.", method, path);
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Cabecera Authorization ausente o incorrecta para la ruta: {}", path);
            return onError(exchange, "Cabecera Authorization ausente o incorrecta", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        String username = null; // email
        Long userId = null;
        List<String> roles = new ArrayList<>();

        try {
            Claims claims = jwtUtils.extractAllClaims(token);
            username = claims.getSubject();
            userId = claims.get("userId", Long.class); // Asegúrate que JwtUtils pueda extraer esto
            roles = claims.get("roles", List.class);
            if (roles == null) roles = new ArrayList<>();

            if (username == null || userId == null) {
                logger.warn("Token JWT válido pero faltan claims necesarios (username o userId) para path: {}", path);
                return onError(exchange, "Token JWT inválido: claims faltantes", HttpStatus.UNAUTHORIZED);
            }

            logger.info("Token JWT decodificado para usuario: {}, ID: {}, roles: {} para path: {}", username, userId, roles, path);

            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Asegúrate que el constructor de UserPrincipal coincida: (Long id, String email, Collection authorities)
            UserPrincipal userPrincipal = new UserPrincipal(userId, username, authorities);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

            logger.info("Autenticación creada para: {}. Pasando al siguiente filtro con contexto de seguridad.", userPrincipal.getUsername());
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));

        } catch (ExpiredJwtException eje) {
            logger.warn("Token JWT expirado para path {}: {}", path, eje.getMessage());
            return onError(exchange, "Token JWT expirado", HttpStatus.UNAUTHORIZED);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error al validar/parsear token JWT para path {}: {}", path, e.getMessage(), e);
            return onError(exchange, "Token JWT inválido o malformado", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.warn("Respondiendo con error desde Gateway: {} - {}", httpStatus, errMessage);
        return response.setComplete();
    }
}