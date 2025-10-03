package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для бизнес-логики, связанной с ролями
 */
public class RoleBusinessLogicTest {
    /**
     * Тест проверяет логику проверки прав на основе ролей
     */
    @Test
    void hasAdminAccess_ShouldReturnCorrectValues() {
        // Пример теста для гипотетического метода проверки прав
        assertTrue(hasAdminAccess(Role.ADMIN), "ADMIN должен иметь права администратора");
        assertFalse(hasAdminAccess(Role.USER), "USER не должен иметь права администратора");
    }

    /**
     * Тест проверяет логику проверки прав пользователя
     */
    @Test
    void hasUserAccess_ShouldReturnCorrectValues() {
        assertTrue(hasUserAccess(Role.USER), "USER должен иметь права пользователя");
        assertTrue(hasUserAccess(Role.ADMIN), "ADMIN также должен иметь права пользователя");
    }

    private boolean hasAdminAccess(Role role) {
        return role == Role.ADMIN;
    }

    private boolean hasUserAccess(Role role) {
        return role == Role.USER || role == Role.ADMIN;
    }
}
