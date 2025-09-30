package ru.skypro.homework.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.dto.Role;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private UserEntity testUser;
    private AdEntity testAd1;
    private AdEntity testAd2;
    private CommentEntity testComment1;
    private CommentEntity testComment2;
    private CommentEntity testComment3;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("+79999999999");
        testUser.setRole(Role.USER);
        testUser = entityManager.persistAndFlush(testUser);

        // Создаем тестовые объявления
        testAd1 = new AdEntity();
        testAd1.setTitle("Test Ad 1");
        testAd1.setDescription("Test Description 1");
        testAd1.setPrice(1000);
        testAd1.setAuthor(testUser);
        testAd1 = entityManager.persistAndFlush(testAd1);

        testAd2 = new AdEntity();
        testAd2.setTitle("Test Ad 2");
        testAd2.setDescription("Test Description 2");
        testAd2.setPrice(2000);
        testAd2.setAuthor(testUser);
        testAd2 = entityManager.persistAndFlush(testAd2);

        // Создаем тестовые комментарии
        testComment1 = new CommentEntity();
        testComment1.setText("First comment");
        testComment1.setCreatedAt(System.currentTimeMillis());
        testComment1.setAd(testAd1);
        testComment1.setAuthor(testUser);
        testComment1 = entityManager.persistAndFlush(testComment1);

        testComment2 = new CommentEntity();
        testComment2.setText("Second comment");
        testComment2.setCreatedAt(System.currentTimeMillis() + 1000);
        testComment2.setAd(testAd1);
        testComment2.setAuthor(testUser);
        testComment2 = entityManager.persistAndFlush(testComment2);

        testComment3 = new CommentEntity();
        testComment3.setText("Third comment");
        testComment3.setCreatedAt(System.currentTimeMillis() + 2000);
        testComment3.setAd(testAd2);
        testComment3.setAuthor(testUser);
        testComment3 = entityManager.persistAndFlush(testComment3);
    }

    /**
     * Тест метода findAllByAd_Id - поиск всех комментариев по ID объявления
     * Проверяет:
     * 1. Возврат всех комментариев для существующего объявления
     * 2. Корректность фильтрации по разным объявлениям
     */
    @Test
    void findAllByAd_Id_ShouldReturnCommentsForSpecificAd() {
        // When
        List<CommentEntity> commentsForAd1 = commentRepository.findAllByAd_Id(testAd1.getId());
        List<CommentEntity> commentsForAd2 = commentRepository.findAllByAd_Id(testAd2.getId());

        // Then
        assertThat(commentsForAd1).hasSize(2);
        assertThat(commentsForAd1)
                .extracting(CommentEntity::getId)
                .containsExactlyInAnyOrder(testComment1.getId(), testComment2.getId());

        assertThat(commentsForAd2).hasSize(1);
        assertThat(commentsForAd2.get(0).getId()).isEqualTo(testComment3.getId());
    }

    /**
     * Тест метода findAllByAd_Id для случая, когда у объявления нет комментариев
     * Проверяет возврат пустого списка
     */
    @Test
    void findAllByAd_Id_ShouldReturnEmptyListWhenNoCommentsForAd() {
        // Given - несуществующий ID объявления
        Integer nonExistentAdId = 999;

        // When - ищем комментарии для несуществующего объявления
        List<CommentEntity> comments = commentRepository.findAllByAd_Id(nonExistentAdId);

        // Then - должен вернуться пустой список
        assertThat(comments).isEmpty();
    }

    /**
     * Тест метода findByIdAndAd_Id - поиск комментария по ID комментария и ID объявления
     * Проверяет успешный поиск существующего комментария
     */
    @Test
    void findByIdAndAd_Id_ShouldReturnCommentWhenExists() {
        // When - ищем комментарий по ID комментария и ID объявления
        Optional<CommentEntity> foundComment = commentRepository
                .findByIdAndAd_Id(testComment1.getId(), testAd1.getId());

        // Then - проверяем, что комментарий найден и содержит правильные данные
        assertThat(foundComment).isPresent(); // Должен присутствовать в Optional
        assertThat(foundComment.get().getId()).isEqualTo(testComment1.getId()); // Проверяем ID
        assertThat(foundComment.get().getText()).isEqualTo("First comment"); // Проверяем текст
        assertThat(foundComment.get().getAd().getId()).isEqualTo(testAd1.getId()); // Проверяем связь с объявлением
    }

    /**
     * Тест метода findByIdAndAd_Id для случая, когда комментарий не существует
     * Проверяет возврат пустого Optional
     */
    @Test
    void findByIdAndAd_Id_ShouldReturnEmptyWhenCommentNotFound() {
        // When - ищем несуществующий комментарий для существующего объявления
        Optional<CommentEntity> foundComment = commentRepository
                .findByIdAndAd_Id(999, testAd1.getId());

        // Then - должен вернуться пустой Optional
        assertThat(foundComment).isEmpty();
    }

    /**
     * Тест метода findByIdAndAd_Id для случая несоответствия ID объявления
     * Проверяет, что комментарий не найден при неверном ID объявления
     */
    @Test
    void findByIdAndAd_Id_ShouldReturnEmptyWhenAdIdDoesNotMatch() {
        // When - ищем существующий комментарий, но с неправильным ID объявления
        Optional<CommentEntity> foundComment = commentRepository
                .findByIdAndAd_Id(testComment1.getId(), testAd2.getId());

        // Then - должен вернуться пустой Optional, т.к. комментарий принадлежит другому объявлению
        assertThat(foundComment).isEmpty();
    }

    /**
     * Тест метода deleteByIdAndAd_Id - удаление комментария по ID комментария и ID объявления
     * Проверяет успешное удаление существующего комментария
     */
    @Test
    void deleteByIdAndAd_Id_ShouldDeleteCommentWhenExists() {
        // Given - ID комментария и объявления для удаления
        Integer commentId = testComment1.getId();
        Integer adId = testAd1.getId();

        // When - удаляем комментарий
        commentRepository.deleteByIdAndAd_Id(commentId, adId);
        entityManager.flush(); // Применяем изменения к БД
        entityManager.clear(); // Очищаем кэш Hibernate

        // Then - проверяем, что комментарий удален
        Optional<CommentEntity> deletedComment = commentRepository.findById(commentId);
        assertThat(deletedComment).isEmpty();

        // Проверяем, что другие комментарии остались нетронутыми
        List<CommentEntity> remainingComments = commentRepository.findAllByAd_Id(adId);
        assertThat(remainingComments).hasSize(1); // Должен остаться один комментарий
        assertThat(remainingComments.get(0).getId()).isEqualTo(testComment2.getId()); // Проверяем ID оставшегося комментария
    }

    /**
     * Тест метода deleteByIdAndAd_Id для случая, когда комментарий не существует
     * Проверяет, что метод не выбрасывает исключение и не изменяет данные
     */
    @Test
    void deleteByIdAndAd_Id_ShouldDoNothingWhenCommentNotFound() {
        // Given - запоминаем начальное количество комментариев
        long initialCount = commentRepository.count();

        // When - пытаемся удалить несуществующий комментарий
        commentRepository.deleteByIdAndAd_Id(999, testAd1.getId());
        entityManager.flush();

        // Then - количество комментариев не должно измениться
        long finalCount = commentRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Тест метода deleteByIdAndAd_Id для случая несоответствия ID объявления
     * Проверяет, что комментарий не удаляется при неверном ID объявления
     */
    @Test
    void deleteByIdAndAd_Id_ShouldDoNothingWhenAdIdDoesNotMatch() {
        // Given - запоминаем начальное количество комментариев
        long initialCount = commentRepository.count();

        // When - пытаемся удалить комментарий с неправильным ID объявления
        commentRepository.deleteByIdAndAd_Id(testComment1.getId(), testAd2.getId());
        entityManager.flush();

        // Then - количество комментариев не должно измениться
        long finalCount = commentRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Тест сохранения нового комментария
     * Проверяет корректность работы метода save()
     */
    @Test
    void save_ShouldPersistNewComment() {
        // Given - создаем новый комментарий
        CommentEntity newComment = new CommentEntity();
        newComment.setText("New comment");
        newComment.setCreatedAt(System.currentTimeMillis());
        newComment.setAd(testAd1);
        newComment.setAuthor(testUser);

        // When - сохраняем комментарий
        CommentEntity savedComment = commentRepository.save(newComment);
        entityManager.flush(); // Применяем изменения к БД
        entityManager.clear(); // Очищаем кэш Hibernate

        // Then - проверяем, что комментарий сохранен с присвоенным ID
        assertThat(savedComment.getId()).isNotNull(); // ID должен быть присвоен

        // Проверяем, что комментарий можно найти по ID
        Optional<CommentEntity> foundComment = commentRepository.findById(savedComment.getId());
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getText()).isEqualTo("New comment"); // Проверяем текст
        assertThat(foundComment.get().getAd().getId()).isEqualTo(testAd1.getId()); // Проверяем связь с объявлением
        assertThat(foundComment.get().getAuthor().getId()).isEqualTo(testUser.getId()); // Проверяем автора
    }

    /**
     * Тест обновления существующего комментария
     * Проверяет корректность работы метода save() для обновления
     */
    @Test
    void update_ShouldModifyExistingComment() {
        // Given - изменяем текст существующего комментария
        String updatedText = "Updated comment text";
        testComment1.setText(updatedText);

        // When - сохраняем изменения
        CommentEntity updatedComment = commentRepository.save(testComment1);
        entityManager.flush(); // Применяем изменения к БД
        entityManager.clear(); // Очищаем кэш Hibernate

        // Then - проверяем, что комментарий обновлен
        Optional<CommentEntity> foundComment = commentRepository.findById(testComment1.getId());
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getText()).isEqualTo(updatedText); // Проверяем обновленный текст
    }
}
