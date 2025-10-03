package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.skypro.homework.dto.Role;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для enum {@link Role}
 * Проверяют корректность определения ролей пользователей в системе
 */
public class RoleTest {

    /**
     * Тест проверяет, что enum содержит все ожидаемые роли
     */
    @Test
    void roleEnum_ShouldContainExpectedValues() {
        // Given - ожидаемые значения ролей
        Role[] expectedRoles = {Role.USER, Role.ADMIN};

        // When - получаем фактические значения
        Role[] actualRoles = Role.values();

        // Then - проверяем соответствие
        assertArrayEquals(expectedRoles, actualRoles,
                "Enum Role должен содержать точно определенные роли USER и ADMIN");
    }

    /**
     * Параметризованный тест проверяет корректность преобразования строк в enum
     */
    @ParameterizedTest
    @EnumSource(Role.class)
    void valueOf_ShouldReturnCorrectRoleForValidName(Role role) {
        // Given - имя роли
        String roleName = role.name();

        // When - преобразуем строку в enum
        Role result = Role.valueOf(roleName);

        // Then - проверяем, что получена правильная роль
        assertEquals(role, result,
                "Метод valueOf должен корректно преобразовывать строку в соответствующую роль");
    }

    /**
     * Тест проверяет, что преобразование невалидной строки вызывает исключение
     */
    @Test
    void valueOf_ShouldThrowExceptionForInvalidName() {
        // Given - невалидное имя роли
        String invalidRoleName = "MODERATOR";

        // When & Then - проверяем, что выбрасывается исключение
        assertThrows(IllegalArgumentException.class,
                () -> Role.valueOf(invalidRoleName),
                "Метод valueOf должен выбрасывать IllegalArgumentException для невалидного имени роли");
    }

    /**
     * Тест проверяет строковое представление ролей
     */
    @Test
    void toString_ShouldReturnRoleName() {
        assertEquals("USER", Role.USER.toString(), "toString() должен возвращать имя роли");
        assertEquals("ADMIN", Role.ADMIN.toString(), "toString() должен возвращать имя роли");
    }

    /**
     * Тест проверяет, что роли правильно сравниваются
     */
    @Test
    void roleEnum_ShouldSupportEqualityOperations() {
        // Проверяем равенство
        assertEquals(Role.USER, Role.USER, "Роли USER должны быть равны");
        assertEquals(Role.ADMIN, Role.ADMIN, "Роли ADMIN должны быть равны");

        // Проверяем неравенство
        assertNotEquals(Role.USER, Role.ADMIN, "Роли USER и ADMIN должны различаться");
        assertNotEquals(Role.ADMIN, Role.USER, "Роли ADMIN и USER должны различаться");
    }

    /**
     * Тест проверяет, что все роли имеют корректные имена в верхнем регистре
     */
    @Test
    void roleNames_ShouldBeUpperCase() {
        for (Role role : Role.values()) {
            String roleName = role.name();
            assertEquals(roleName.toUpperCase(), roleName,
                    "Имя роли " + role + " должно быть в верхнем регистре");
        }
    }
}

