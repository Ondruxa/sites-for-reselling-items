package ru.skypro.homework.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Profile("test")
@Configuration
@EnableWebSecurity
public class TestSecurityConfig {
    private static final String[] AUTH_WHITELIST = {
            "/ads",
            "/ads/",
            "/ads/*"  // Только просмотр объявлений публичный
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .mvcMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated() // Все остальное требует аутентификации
                )
                .httpBasic(withDefaults())
                .build();
    }
}
