package com.app.projectbar.config;

import com.app.projectbar.domain.enums.RoleEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .cors().and()
                .authorizeHttpRequests()
                .requestMatchers( "/api/ingredient/**").hasAnyRole(RoleEnum.ADMIN.name())
                .requestMatchers("/api/inventory/**").hasRole(RoleEnum.ADMIN.name())
                .requestMatchers("/api/order/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                .requestMatchers("/api/product/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.WAITER.name())
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic();

        return httpSecurity.build();

    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

