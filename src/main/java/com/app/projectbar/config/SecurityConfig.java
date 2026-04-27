package com.app.projectbar.config;

import com.app.projectbar.domain.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests((authorize) -> authorize

                        // ==================== PÚBLICOS (Sin autenticación) ====================

                        // Swagger - Documentación API
                        .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // Productos - Los clientes pueden ver el menú sin autenticarse
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // Órdenes - Los clientes pueden crear órdenes vía QR (SELF_SERVICE)
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()

                        // WebSocket endpoints - permitir sin autenticación (JWT va en headers)
                        .requestMatchers("/ws/**").permitAll()

                        // ==================== AUTENTICACIÓN ====================
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // ==================== INGREDIENTES ====================
                        // Ver ingredientes: Admin y Chef
                        .requestMatchers(HttpMethod.GET, "/api/ingredients/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.CHEF.name())
                        // Crear, modificar, eliminar ingredientes: Solo Admin
                        .requestMatchers(HttpMethod.POST, "/api/ingredients/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/ingredients/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/ingredients/**").hasRole(Role.ADMIN.name())

                        // ==================== PRODUCTOS ====================
                        // Crear productos: Solo Admin
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole(Role.ADMIN.name())
                        // Modificar productos: Solo Admin
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole(Role.ADMIN.name())
                        // Eliminar productos: Solo Admin
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole(Role.ADMIN.name())

                        // ==================== INVENTARIO ====================
                        // Ver inventario: Admin, Bartender, Waiter, Chef
                        .requestMatchers(HttpMethod.GET, "/api/inventory/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.BARTENDER.name(), Role.WAITER.name(), Role.CHEF.name())
                        // Modificar inventario: Solo Admin
                        .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/inventory/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole(Role.ADMIN.name())

                        // ==================== ÓRDENES ====================
                        // Ver órdenes: Admin, Bartender, Waiter, Chef
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.BARTENDER.name(), Role.WAITER.name(), Role.CHEF.name())
                        // Asignar mesero a orden: Solo Admin
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/waiter/**").hasRole(Role.ADMIN.name())
                        // Agregar items a orden: Admin y Waiter
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/items")
                        .hasAnyRole(Role.ADMIN.name(), Role.WAITER.name())
                        // Remover items de orden: Admin y Waiter
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/*/items/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.WAITER.name())
                        // Cambiar estado de orden: Admin, Bartender, Waiter, Chef
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status")
                        .hasAnyRole(Role.ADMIN.name(), Role.BARTENDER.name(), Role.WAITER.name(), Role.CHEF.name())
                        // Actualizar orden: Admin y Waiter
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.WAITER.name())
                        // Eliminar orden: Solo Admin
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole(Role.ADMIN.name())

                        // ==================== MESEROS ====================
                        // Ver meseros con órdenes activas: Solo Admin
                        .requestMatchers(HttpMethod.GET, "/api/waiters/**").hasRole(Role.ADMIN.name())

                        // ==================== FACTURAS (BILL) ====================
                        // Ver facturas: Admin
                        .requestMatchers(HttpMethod.GET, "/api/bills/**").hasRole(Role.ADMIN.name())
                        // Crear facturas: Admin y Waiter
                        .requestMatchers(HttpMethod.POST, "/api/bills/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.WAITER.name())

                        // ==================== MESAS (TABLES) ====================
                        // Ver mesas: Todos los roles autenticados
                        .requestMatchers(HttpMethod.GET, "/api/tables/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.BARTENDER.name(), Role.WAITER.name(), Role.CHEF.name())
                        // Cambiar estado de mesa: Admin y Waiter
                        .requestMatchers(HttpMethod.PATCH, "/api/tables/**/status")
                        .hasAnyRole(Role.ADMIN.name(), Role.WAITER.name())
                        // Crear, modificar, eliminar mesas: Solo Admin
                        .requestMatchers(HttpMethod.POST, "/api/tables/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/tables/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/tables/**").hasRole(Role.ADMIN.name())

                        // ==================== USUARIOS ====================
                        // Gestión de usuarios: Solo Admin
                        .requestMatchers("/api/user/**").hasRole(Role.ADMIN.name())

                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:5173",
                "http://localhost:5174", "http://localhost:3000")); // Orígenes permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // Métodos
                                                                                                      // permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Encabezados permitidos
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
