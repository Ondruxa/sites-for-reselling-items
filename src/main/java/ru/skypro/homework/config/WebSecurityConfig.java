package ru.skypro.homework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Центральная конфигурация безопасности приложения.
 * <p>
 * Основные задачи:
 * <ul>
 *   <li>Определяет (whitelist) эндпоинты.</li>
 *   <li>Разрешает анонимный доступ к GET /ads (список, карточка) для публичного просмотра.</li>
 *   <li>Требует роль USER или ADMIN для эндпоинтов /ads/** и /users/**.</li>
 *   <li>Включает HTTP Basic (для простоты тестирования) + CORS (для фронта на http://localhost:3000).</li>
 *   <li>Явно разрешает preflight OPTIONS для всех путей.</li>
 * </ul>
 */
@Configuration
public class WebSecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/webjars/**",
            "/login",
            "/register",
            "/images/**"
    };

    private final UserDetailsService userDetailsService;

    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Основная цепочка фильтров Spring Security.
     * @param http HttpSecurity DSL
     * @return настроенный {@link SecurityFilterChain}
     * @throws Exception при ошибках конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .mvcMatchers(HttpMethod.OPTIONS, "/**").permitAll() // разрешаем preflight для любых путей
                        .mvcMatchers(AUTH_WHITELIST).permitAll()
                        .mvcMatchers(HttpMethod.GET, "/ads", "/ads/", "/ads/*").permitAll()
                        .mvcMatchers("/ads/**").hasAnyAuthority("USER", "ADMIN")
                        .mvcMatchers("/users/**").hasAnyAuthority("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .cors(withDefaults())
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * Конфигурация CORS для взаимодействия с фронтендом (localhost:3000).
     * @return источник CORS-конфигурации
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
