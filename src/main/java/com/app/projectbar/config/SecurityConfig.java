package com.app.projectbar.config;

import com.app.projectbar.domain.enums.RoleEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests((authorize) -> authorize

                // ==================== PÚBLICOS (Sin autenticación) ====================
                
                // Swagger - Documentación API
                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                
                // Productos - Los clientes pueden ver el menú sin autenticarse
                .requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()
                
                // Órdenes - Los clientes pueden crear órdenes vía QR (SELF_SERVICE)
                .requestMatchers(HttpMethod.POST, "/api/order/save").permitAll()

                // ==================== INGREDIENTES ====================
                // Ver ingredientes: Admin y Chef
                .requestMatchers(HttpMethod.GET, "/api/ingredient/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.CHEF.name())
                // Crear, modificar, eliminar ingredientes: Solo Admin
                .requestMatchers(HttpMethod.POST, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.PUT, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())

                // ==================== PRODUCTOS ====================
                // Crear productos: Solo Admin
                .requestMatchers(HttpMethod.POST, "/api/product/**").hasRole(RoleEnum.ADMIN.name())
                // Modificar productos: Solo Admin
                .requestMatchers(HttpMethod.PUT, "/api/product/**").hasRole(RoleEnum.ADMIN.name())
                // Eliminar productos: Solo Admin
                .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasRole(RoleEnum.ADMIN.name())

                // ==================== INVENTARIO ====================
                // Ver inventario: Admin, Bartender, Waiter, Chef
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                // Modificar inventario: Solo Admin
                .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole(RoleEnum.ADMIN.name())

                // ==================== ÓRDENES ====================
                // Ver órdenes: Admin, Bartender, Waiter, Chef
                .requestMatchers(HttpMethod.GET, "/api/order/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                // Agregar items a orden: Admin y Waiter
                .requestMatchers(HttpMethod.PUT, "/api/order/add-order-item/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                // Remover items de orden: Admin y Waiter
                .requestMatchers(HttpMethod.PUT, "/api/order/remove-order-item/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                // Cambiar estado de orden: Admin, Bartender, Waiter, Chef
                .requestMatchers(HttpMethod.PUT, "/api/order/change-status/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                // Actualizar orden: Admin y Waiter
                .requestMatchers(HttpMethod.PUT, "/api/order/update").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                // Eliminar orden: Solo Admin
                .requestMatchers(HttpMethod.DELETE, "/api/order/**").hasRole(RoleEnum.ADMIN.name())

                // ==================== FACTURAS (BILL) ====================
                // Ver facturas: Admin
                .requestMatchers(HttpMethod.GET, "/api/bill/**").hasRole(RoleEnum.ADMIN.name())
                // Crear facturas: Admin y Waiter
                .requestMatchers(HttpMethod.POST, "/api/bill/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())

                // ==================== USUARIOS ====================
                // Gestión de usuarios: Solo Admin
                .requestMatchers("/api/user/**").hasRole(RoleEnum.ADMIN.name())

                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return httpSecurity.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:5173", "http://localhost:5174", "http://localhost:3000")); // Orígenes permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Encabezados permitidos
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
