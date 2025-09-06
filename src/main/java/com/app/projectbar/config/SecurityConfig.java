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
                .authorizeHttpRequests( (authorize) -> authorize
                // INGREDIENTES
                        .requestMatchers("/api/ingredient/**").permitAll()
                 /*
                .requestMatchers(HttpMethod.GET, "/api/ingredient/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.CHEF.name())
                .requestMatchers(HttpMethod.PUT, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())
                */
                // PRODUCTOS
                .requestMatchers( "/api/product/**").permitAll()
                        /*
                .requestMatchers(HttpMethod.PUT, "/api/product/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.GET, "/api/product/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasRole(RoleEnum.ADMIN.name())
                */


                // INVENTORY
                        .requestMatchers( "/api/inventory/**").permitAll()
                        /*
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                .requestMatchers("/api/inventory/**").hasRole(RoleEnum.ADMIN.name())

                         */

                //ORDER
                .requestMatchers(HttpMethod.POST, "/api/order/**").permitAll()
                /*
                .requestMatchers(HttpMethod.GET, "/api/order/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                .requestMatchers(HttpMethod.PUT, "/api/order/add-order-item/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                .requestMatchers(HttpMethod.PUT, "/api/order/remove-order-item/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                .requestMatchers(HttpMethod.PUT, "/api/order/change-status/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
*/
                //BILL
                .requestMatchers("/api/bill/**").permitAll()

                // SWAGGER
                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                .anyRequest()
                        //.authenticated()
                .permitAll()) // Permitir todas las solicitudes sin autenticación
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
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://example.com")); // Orígenes permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Encabezados permitidos
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
