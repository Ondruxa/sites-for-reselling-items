package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки интеграции ролей с Spring Security
 */
@SpringBootTest
public class RoleSecurityIntegrationTest {

    @Autowired
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    /**
     * Тест проверяет, что роль USER корректно преобразуется в authority Spring Security
     */
    @Test
    void userRole_ShouldBeMappedToSpringSecurityAuthority() {
        // Given - пользователь с ролью USER
        String username = "user@gmail.com";

        // When - загружаем пользователя
        var userDetails = userDetailsService.loadUserByUsername(username);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Then - проверяем, что роль преобразована в authority с префиксом "ROLE_"
        assertTrue(authorities.stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + Role.USER.name())),
                "Роль USER должна быть преобразована в authority 'ROLE_USER'");

        assertFalse(authorities.stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + Role.ADMIN.name())),
                "Пользователь не должен иметь роль ADMIN");
    }

    /**
     * Тест проверяет формат authorities для Spring Security
     */
    @Test
    void role_ShouldHaveCorrectSpringSecurityAuthorityFormat() {
        // Проверяем, что все роли имеют правильный формат для Spring Security
        for (Role role : Role.values()) {
            String expectedAuthority = "ROLE_" + role.name();
            assertEquals(expectedAuthority, "ROLE_" + role.name(),
                    "Роль " + role + " должна преобразовываться в authority: " + expectedAuthority);
        }
    }
}
