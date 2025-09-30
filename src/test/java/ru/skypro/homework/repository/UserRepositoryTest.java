package ru.skypro.homework.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.dto.Role;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser1;
    private UserEntity testUser2;
    private UserEntity testUser3;

    @BeforeEach
    void setUp() {
        testUser1 = new UserEntity();
        testUser1.setEmail("user1@example.com");
        testUser1.setPassword("password1");
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setPhone("+79999999991");
        testUser1.setRole(Role.USER);
        testUser1.setImage("image1.jpg");
        testUser1 = entityManager.persistAndFlush(testUser1);

        testUser2 = new UserEntity();
        testUser2.setEmail("user2@example.com");
        testUser2.setPassword("password2");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setPhone("+79999999992");
        testUser2.setRole(Role.ADMIN);
        testUser2.setImage("image2.jpg");
        testUser2 = entityManager.persistAndFlush(testUser2);

        testUser3 = new UserEntity();
        testUser3.setEmail("user3@example.com");
        testUser3.setPassword("password3");
        testUser3.setFirstName("Bob");
        testUser3.setLastName("Johnson");
        testUser3.setPhone("+79999999993");
        testUser3.setRole(Role.USER);
        testUser3.setImage("image3.jpg");
        testUser3 = entityManager.persistAndFlush(testUser3);
    }

    /**
     * Тест метода findByEmail - поиск пользователя по email
     * Проверяет успешный поиск существующего пользователя
     */
    @Test
    void findByEmail_ShouldReturnUserWhenEmailExists() {
        // When - ищем пользователя по существующему email
        Optional<UserEntity> foundUser = userRepository.findByEmail("user1@example.com");

        // Then - проверяем, что пользователь найден и данные корректны
        assertThat(foundUser).isPresent(); // Должен присутствовать в Optional
        assertThat(foundUser.get().getId()).isEqualTo(testUser1.getId()); // Проверяем ID
        assertThat(foundUser.get().getEmail()).isEqualTo("user1@example.com"); // Проверяем email
        assertThat(foundUser.get().getFirstName()).isEqualTo("John"); // Проверяем имя
        assertThat(foundUser.get().getLastName()).isEqualTo("Doe"); // Проверяем фамилию
        assertThat(foundUser.get().getRole()).isEqualTo(Role.USER); // Проверяем роль
    }

    /**
     * Тест метода findByEmail для случая, когда пользователь не существует
     * Проверяет возврат пустого Optional при поиске несуществующего email
     */
    @Test
    void findByEmail_ShouldReturnEmptyWhenEmailDoesNotExist() {
        // When - ищем пользователя по несуществующему email
        Optional<UserEntity> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then - должен вернуться пустой Optional
        assertThat(foundUser).isEmpty();
    }

    /**
     * Тест метода findByEmail для проверки регистронезависимости поиска
     * Проверяет, что поиск по email чувствителен к регистру (стандартное поведение JPA)
     */
    @Test
    void findByEmail_ShouldBeCaseSensitive() {
        // When - ищем пользователя по email в другом регистре
        Optional<UserEntity> foundUser = userRepository.findByEmail("USER1@EXAMPLE.COM");

        // Then - должен вернуться пустой Optional, т.к. поиск чувствителен к регистру
        assertThat(foundUser).isEmpty();
    }

    /**
     * Тест метода save - сохранение нового пользователя
     * Проверяет корректность создания новой записи в базе данных
     */
    @Test
    void save_ShouldPersistNewUser() {
        // Given - создаем нового пользователя
        UserEntity newUser = new UserEntity();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("newpassword");
        newUser.setFirstName("Alice");
        newUser.setLastName("Brown");
        newUser.setPhone("+79999999994");
        newUser.setRole(Role.USER);
        newUser.setImage("new_image.jpg");

        // When - сохраняем пользователя
        UserEntity savedUser = userRepository.save(newUser);
        entityManager.flush(); // Применяем изменения к БД
        entityManager.clear(); // Очищаем кэш Hibernate

        // Then - проверяем, что пользователь сохранен с присвоенным ID
        assertThat(savedUser.getId()).isNotNull(); // ID должен быть присвоен

        // Проверяем, что пользователь можно найти по ID
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("newuser@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("Alice");
        assertThat(foundUser.get().getLastName()).isEqualTo("Brown");
        assertThat(foundUser.get().getPhone()).isEqualTo("+79999999994");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.USER);
    }

    /**
     * Тест метода save - обновление существующего пользователя
     * Проверяет корректность обновления данных пользователя
     */
    @Test
    void save_ShouldUpdateExistingUser() {
        // Given - изменяем данные существующего пользователя
        testUser1.setFirstName("UpdatedFirstName");
        testUser1.setLastName("UpdatedLastName");
        testUser1.setPhone("+78888888888");
        testUser1.setRole(Role.ADMIN);

        // When - сохраняем изменения
        UserEntity updatedUser = userRepository.save(testUser1);
        entityManager.flush(); // Применяем изменения к БД
        entityManager.clear(); // Очищаем кэш Hibernate

        // Then - проверяем, что пользователь обновлен
        Optional<UserEntity> foundUser = userRepository.findById(testUser1.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(foundUser.get().getLastName()).isEqualTo("UpdatedLastName");
        assertThat(foundUser.get().getPhone()).isEqualTo("+78888888888");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.ADMIN);
        // Проверяем, что email остался неизменным
        assertThat(foundUser.get().getEmail()).isEqualTo("user1@example.com");
    }

    /**
     * Тест метода findById - поиск пользователя по ID
     * Проверяет успешный поиск существующего пользователя
     */
    @Test
    void findById_ShouldReturnUserWhenExists() {
        // When - ищем пользователя по существующему ID
        Optional<UserEntity> foundUser = userRepository.findById(testUser2.getId());

        // Then - проверяем, что пользователь найден
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("user2@example.com");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.ADMIN);
    }

    /**
     * Тест метода findById для случая, когда пользователь не существует
     * Проверяет возврат пустого Optional при поиске несуществующего ID
     */
    @Test
    void findById_ShouldReturnEmptyWhenUserDoesNotExist() {
        // When - ищем пользователя по несуществующему ID
        Optional<UserEntity> foundUser = userRepository.findById(999);

        // Then - должен вернуться пустой Optional
        assertThat(foundUser).isEmpty();
    }

    /**
     * Тест метода findAll - получение всех пользователей
     * Проверяет корректность возврата полного списка пользователей
     */
    @Test
    void findAll_ShouldReturnAllUsers() {
        // When - получаем всех пользователей
        List<UserEntity> allUsers = userRepository.findAll();

        // Then - проверяем, что возвращены все созданные пользователи
        assertThat(allUsers).hasSize(3); // Должно быть 3 пользователя
        assertThat(allUsers)
                .extracting(UserEntity::getEmail)
                .containsExactlyInAnyOrder(
                        "user1@example.com",
                        "user2@example.com",
                        "user3@example.com"
                ); // Проверяем email всех пользователей
    }

    /**
     * Тест метода deleteById - удаление пользователя по ID
     * Проверяет успешное удаление существующего пользователя
     */
    @Test
    void deleteById_ShouldDeleteUserWhenExists() {
        // Given - ID пользователя для удаления
        Integer userId = testUser1.getId();
        long initialCount = userRepository.count();

        // When - удаляем пользователя
        userRepository.deleteById(userId);
        entityManager.flush(); // Применяем изменения к БД
        entityManager.clear(); // Очищаем кэш Hibernate

        // Then - проверяем, что пользователь удален
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();

        // Проверяем, что общее количество пользователей уменьшилось
        long finalCount = userRepository.count();
        assertThat(finalCount).isEqualTo(initialCount - 1);
    }

    /**
     * Тест метода deleteById для случая, когда пользователь не существует
     * Проверяет, что метод выбрасывает исключение при удалении несуществующего пользователя
     */
    @Test
    void deleteById_ShouldThrowExceptionWhenUserDoesNotExist() {
        // Given - запоминаем начальное количество пользователей
        long initialCount = userRepository.count();
        Integer nonExistentUserId = 999;

        // When & Then - проверяем, что при удалении несуществующего пользователя выбрасывается исключение
        assertThatThrownBy(() -> userRepository.deleteById(nonExistentUserId))
                .isInstanceOf(org.springframework.dao.EmptyResultDataAccessException.class);

        // Then - количество пользователей не должно измениться
        long finalCount = userRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Тест уникальности поля email
     * Проверяет, что невозможно сохранить двух пользователей с одинаковым email
     */
    @Test
    void save_ShouldThrowExceptionWhenEmailAlreadyExists() {
        // Given - создаем пользователя с уже существующим email
        UserEntity duplicateUser = new UserEntity();
        duplicateUser.setEmail("user1@example.com"); // Email уже существует
        duplicateUser.setPassword("password");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");
        duplicateUser.setPhone("+79999999995");
        duplicateUser.setRole(Role.USER);

        // When & Then - проверяем, что при сохранении возникает исключение
        // Из-за ограничения уникальности на поле email
        assertThatThrownBy(() -> {
            userRepository.save(duplicateUser);
            entityManager.flush(); // Применяем изменения к БД
        })
                .isInstanceOf(javax.persistence.PersistenceException.class)
                .hasCauseInstanceOf(org.hibernate.exception.ConstraintViolationException.class)
                .hasMessageContaining("could not execute statement");
    }

    /**
     * Тест обязательных полей (nullable = false)
     * Проверяет, что невозможно сохранить пользователя без обязательных полей
     */
    @Test
    void save_ShouldThrowExceptionWhenRequiredFieldsAreNull() {
        // Given - создаем пользователя без обязательных полей
        UserEntity invalidUser = new UserEntity();
        // Не устанавливаем email (nullable = false)
        invalidUser.setPassword("password"); // password установлен
        invalidUser.setFirstName("Test");
        invalidUser.setLastName("User");
        invalidUser.setRole(Role.USER); // role установлен

        // When & Then - проверяем, что при сохранении возникает исключение
        try {
            userRepository.save(invalidUser);
            entityManager.flush(); // Применяем изменения к БД
            // Если дошли до этой точки, тест должен упасть
            org.junit.jupiter.api.Assertions.fail("Expected constraint violation exception");
        } catch (Exception e) {
            // Ожидаем исключение связанное с нарушением ограничения not-null
            assertThat(e).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
    }

    /**
     * Тест поиска по разным ролям пользователей
     * Демонстрирует использование стандартных методов JpaRepository для фильтрации
     */
    @Test
    void findByRole_ShouldReturnUsersWithSpecificRole() {
        // When - получаем всех пользователей и фильтруем по роли (в тесте)
        List<UserEntity> allUsers = userRepository.findAll();
        List<UserEntity> adminUsers = allUsers.stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .collect(java.util.stream.Collectors.toList()); // Исправлено для Java 11
        List<UserEntity> regularUsers = allUsers.stream()
                .filter(user -> user.getRole() == Role.USER)
                .collect(java.util.stream.Collectors.toList()); // Исправлено для Java 11

        // Then - проверяем корректность фильтрации по ролям
        assertThat(adminUsers).hasSize(1); // Должен быть 1 администратор
        assertThat(adminUsers.get(0).getEmail()).isEqualTo("user2@example.com");

        assertThat(regularUsers).hasSize(2); // Должно быть 2 обычных пользователя
        assertThat(regularUsers)
                .extracting(UserEntity::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user3@example.com");
    }
}
