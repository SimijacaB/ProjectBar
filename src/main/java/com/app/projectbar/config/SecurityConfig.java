package com.app.projectbar.config;

import com.app.projectbar.domain.enums.Permission;
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

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests( (authorize) -> authorize
                // INGREDIENTES
                .requestMatchers(HttpMethod.GET, "/api/ingredient/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.CHEF.name())
                .requestMatchers(HttpMethod.PUT, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE, "/api/ingredient/**").hasRole(RoleEnum.ADMIN.name())

                // PRODUCTOS
                .requestMatchers(HttpMethod.POST, "/api/product/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.PUT, "/api/product/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers(HttpMethod.GET, "/api/product/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasRole(RoleEnum.ADMIN.name())

                // INVENTORY
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                .requestMatchers("/api/inventory/**").hasRole(RoleEnum.ADMIN.name())

                //ORDER
                .requestMatchers(HttpMethod.POST, "/api/order/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name())
                .requestMatchers(HttpMethod.GET, "/api/order/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())
                .requestMatchers(HttpMethod.PUT, "/api/order/add-order-item/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                .requestMatchers(HttpMethod.PUT, "/api/order/remove-order-item/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                .requestMatchers(HttpMethod.PUT, "/api/order/change-status/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.BARTENDER.name(), RoleEnum.WAITER.name(), RoleEnum.CHEF.name())

                .anyRequest()
                .authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return httpSecurity.build();

    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

