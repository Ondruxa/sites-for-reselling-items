package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private UserServiceImpl userService;

    private NewPassword validPassword;
    private UpdateUser validUpdateUser;
    private MultipartFile validImage;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();

        validPassword = new NewPassword();
        validPassword.setCurrentPassword("currentPassword123");
        validPassword.setNewPassword("newSecurePassword456");

        validUpdateUser = new UpdateUser();
        validUpdateUser.setFirstName("Ivan");      // Длина 4 - соответствует ограничениям
        validUpdateUser.setLastName("Ivanov");     // Длина 6 - соответствует ограничениям
        validUpdateUser.setPhone("+79991234567");  // Корректный формат телефона

        // Создаем валидный тестовый файл изображения
        validImage = new MockMultipartFile(
                "profileImage",
                "avatar.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5} // Минимальный контент
        );
    }

    /**
     * Тест установки пароля с валидными данными
     * Проверяем, что метод выполняется без исключений при корректных паролях
     */
    @Test
    void setPassword_WithValidData_ShouldExecuteSuccessfully() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> userService.setPassword(validPassword),
                "Метод setPassword должен выполняться без исключений при валидных данных");
    }

    /**
     * Тест установки пароля с null объектом
     * Проверяем устойчивость к null параметрам
     */
    @Test
    void setPassword_WithNullParameter_ShouldExecuteWithoutErrors() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> userService.setPassword(null),
                "Метод setPassword должен обрабатывать null параметр без исключений");
    }

    /**
     * Тест установки пароля с частично заполненным объектом
     */
    @Test
    void setPassword_WithPartialData_ShouldExecuteSuccessfully() {
        // Arrange
        NewPassword partialPassword = new NewPassword();
        partialPassword.setCurrentPassword("onlyCurrent");

        // Act & Assert
        assertDoesNotThrow(() -> userService.setPassword(partialPassword),
                "Метод setPassword должен работать с частично заполненными объектами");
    }

    /**
     * Тест установки пароля с пустыми строками
     */
    @Test
    void setPassword_WithEmptyStrings_ShouldExecuteSuccessfully() {
        // Arrange
        NewPassword emptyPassword = new NewPassword();
        emptyPassword.setCurrentPassword("");
        emptyPassword.setNewPassword("");

        // Act & Assert
        assertDoesNotThrow(() -> userService.setPassword(emptyPassword),
                "Метод setPassword должен обрабатывать пустые строки паролей");
    }

    /**
     * Тест получения пользователя - проверяем контракт метода
     * В текущей реализации всегда возвращается null
     */
    @Test
    void getUser_ShouldAlwaysReturnNull() {
        // Act
        User result = userService.getUser();

        // Assert
        assertNull(result, "Метод getUser() должен возвращать null в соответствии с текущей реализацией");
    }

    /**
     * Тест обновления пользователя с валидными данными
     * Проверяем корректность обработки данных, соответствующих ограничениям
     */
    @Test
    void updateUser_WithValidData_ShouldReturnNullAsPerImplementation() {
        // Act
        UpdateUser result = userService.updateUser(validUpdateUser);

        // Assert
        assertNull(result, "Метод updateUser() должен возвращать null в соответствии с текущей реализацией");
    }

    /**
     * Тест обновления пользователя с граничными значениями имени/фамилии
     */
    @Test
    void updateUser_WithBoundaryLengthNames_ShouldHandleCorrectly() {
        // Arrange
        UpdateUser boundaryUser = new UpdateUser();
        boundaryUser.setFirstName("Iva");      // Минимальная длина 3
        boundaryUser.setLastName("Petrovsky"); // Максимальная длина 10
        boundaryUser.setPhone("+71234567890");

        // Act
        UpdateUser result = userService.updateUser(boundaryUser);

        // Assert
        assertNull(result, "Метод должен возвращать null даже для граничных значений");
    }

    /**
     * Тест обновления пользователя с null параметром
     */
    @Test
    void updateUser_WithNullParameter_ShouldReturnNull() {
        // Act
        UpdateUser result = userService.updateUser(null);

        // Assert
        assertNull(result, "Метод updateUser() должен возвращать null при null параметре");
    }

    /**
     * Тест обновления пользователя с частично заполненными данными
     */
    @Test
    void updateUser_WithPartialData_ShouldHandleCorrectly() {
        // Arrange
        UpdateUser partialUser = new UpdateUser();
        partialUser.setFirstName("Partial"); // Только имя заполнено

        // Act
        UpdateUser result = userService.updateUser(partialUser);

        // Assert
        assertNull(result, "Метод должен возвращать null для частично заполненных объектов");
    }

    /**
     * Тест обновления изображения с валидным файлом
     * Проверяем успешный HTTP ответ
     */
    @Test
    void updateUserImage_WithValidImage_ShouldReturnOkResponse() {
        // Act
        ResponseEntity<Void> response = userService.updateUserImage(validImage);

        // Assert
        assertNotNull(response, "Ответ не должен быть null");
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Должен возвращаться статус 200 OK при валидном изображении");
        assertNull(response.getBody(), "Тело ответа должно быть null");
    }

    /**
     * Тест обновления изображения с null файлом
     * Проверяем устойчивость к null параметрам
     */
    @Test
    void updateUserImage_WithNullFile_ShouldReturnOkResponse() {
        // Act
        ResponseEntity<Void> response = userService.updateUserImage(null);

        // Assert
        assertNotNull(response, "Ответ не должен быть null даже при null файле");
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Должен возвращаться статус 200 OK даже при null файле");
    }

    /**
     * Тест обновления изображения с пустым файлом
     */
    @Test
    void updateUserImage_WithEmptyFile_ShouldReturnOkResponse() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "empty",
                "empty.jpg",
                "image/jpeg",
                new byte[0] // Пустой контент
        );

        // Act
        ResponseEntity<Void> response = userService.updateUserImage(emptyFile);

        // Assert
        assertNotNull(response, "Ответ не должен быть null при пустом файле");
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Должен возвращаться статус 200 OK при пустом файле");
    }

    /**
     * Тест обновления изображения с большим файлом
     * Проверяем обработку файлов разного размера
     */
    @Test
    void updateUserImage_WithLargeFileContent_ShouldReturnOkResponse() {
        // Arrange
        byte[] largeContent = new byte[1024]; // 1KB content
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        MultipartFile largeFile = new MockMultipartFile(
                "large",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        // Act
        ResponseEntity<Void> response = userService.updateUserImage(largeFile);

        // Assert
        assertNotNull(response, "Ответ не должен быть null для большого файла");
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Должен возвращаться статус 200 OK для файлов любого размера");
    }

    /**
     * Тест последовательного выполнения операций пользователя
     * Проверяем интеграцию между методами сервиса
     */
    @Test
    void userServiceWorkflow_ShouldExecuteAllMethodsSuccessfully() {
        // Act & Assert - проверяем полный workflow
        assertDoesNotThrow(() -> {
            // 1. Получаем пользователя (возвращает null)
            User user = userService.getUser();
            assertNull(user, "Get user should return null");

            // 2. Обновляем пароль
            userService.setPassword(validPassword);

            // 3. Обновляем данные пользователя
            UpdateUser updated = userService.updateUser(validUpdateUser);
            assertNull(updated, "Update user should return null");

            // 4. Обновляем аватар
            ResponseEntity<Void> imageResponse = userService.updateUserImage(validImage);
            assertEquals(HttpStatus.OK, imageResponse.getStatusCode(),
                    "Image update should return OK");
        }, "Полный workflow пользователя должен выполняться без исключений");
    }

    /**
     * Тест многократного вызова методов для проверки стабильности
     */
    @Test
    void repeatedServiceCalls_ShouldBeStable() {
        // Act & Assert
        for (int i = 0; i < 5; i++) {
            userService.setPassword(validPassword);
            assertNull(userService.getUser());
            assertNull(userService.updateUser(validUpdateUser));

            ResponseEntity<Void> response = userService.updateUserImage(validImage);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    /**
     * Тест с различными форматами телефонных номеров
     */
    @Test
    void updateUser_WithDifferentPhoneFormats_ShouldHandleAll() {
        // Arrange
        String[] phoneFormats = {
                "+79991234567",
                "+78005553535",
                "+74951234567",
                "+71112223344"
        };

        for (String phone : phoneFormats) {
            UpdateUser userWithPhone = new UpdateUser();
            userWithPhone.setFirstName("Test");
            userWithPhone.setLastName("User");
            userWithPhone.setPhone(phone);

            // Act
            UpdateUser result = userService.updateUser(userWithPhone);

            // Assert
            assertNull(result, "Метод должен возвращать null для любого формата телефона: " + phone);
        }
    }
}
