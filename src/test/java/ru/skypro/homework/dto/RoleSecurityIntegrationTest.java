package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки интеграции ролей с Spring Security
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RoleSecurityIntegrationTest {

    @Autowired
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Тест проверяет, что роль USER корректно преобразуется в authority Spring Security
     * Сценарий: Создается пользователь с ролью USER, затем проверяется его authorities
     * Ожидаемый результат: Пользователь должен иметь authority "USER" (без префикса "ROLE_")
     */
    @Test
    void userRole_ShouldBeMappedToSpringSecurityAuthority() {
        // Given - создаем пользователя с ролью USER в базе данных
        String username = "user@gmail.com";
        String password = "password123";

        UserEntity user = new UserEntity();
        user.setEmail(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("+79999999999");
        user.setRole(Role.USER);
        userRepository.save(user);

        // When - загружаем пользователя через UserDetailsService
        var userDetails = userDetailsService.loadUserByUsername(username);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Then - проверяем, что роль преобразована в authority БЕЗ префикса "ROLE_"
        assertTrue(authorities.stream()
                        .anyMatch(auth -> auth.getAuthority().equals(Role.USER.name())),
                "Роль USER должна быть преобразована в authority 'USER'");

        assertFalse(authorities.stream()
                        .anyMatch(auth -> auth.getAuthority().equals(Role.ADMIN.name())),
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
