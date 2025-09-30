package ru.skypro.homework.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.UserEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AdRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;
    private AdEntity testAd;

    @BeforeEach
    void setUp() {

        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("+79991234567");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser = entityManager.persistAndFlush(testUser);

        testAd = new AdEntity();
        testAd.setTitle("Test Ad");
        testAd.setDescription("Test Description");
        testAd.setPrice(1000);
        testAd.setAuthor(testUser);
        testAd = entityManager.persistAndFlush(testAd);
    }

    /**
     * Тест проверяет базовое сохранение и загрузку объявления по ID
     */
    @Test
    void findById_ShouldReturnAdWhenExists() {
        // When - ищем объявление по ID
        Optional<AdEntity> foundAd = adRepository.findById(testAd.getId());

        // Then - проверяем, что объявление найдено и данные корректны
        assertTrue(foundAd.isPresent(), "Объявление должно быть найдено по ID");
        assertEquals(testAd.getTitle(), foundAd.get().getTitle(), "Заголовок должен совпадать");
        assertEquals(testAd.getDescription(), foundAd.get().getDescription(), "Описание должно совпадать");
        assertEquals(testAd.getPrice(), foundAd.get().getPrice(), "Цена должна совпадать");
        assertEquals(testAd.getAuthor().getId(), foundAd.get().getAuthor().getId(), "Автор должен совпадать");
    }

    /**
     * Тест проверяет поиск всех объявлений конкретного автора
     */
    @Test
    void findAllByAuthorId_ShouldReturnAdsForSpecificAuthor() {
        // Given - создаем второго пользователя и его объявление
        UserEntity anotherUser = new UserEntity();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("Jane");
        anotherUser.setLastName("Smith");
        anotherUser.setPhone("+79997654321");
        anotherUser.setPassword("password");
        anotherUser.setRole(Role.USER);
        anotherUser = entityManager.persistAndFlush(anotherUser);

        AdEntity anotherAd = new AdEntity();
        anotherAd.setTitle("Another Ad");
        anotherAd.setDescription("Another Description");
        anotherAd.setPrice(2000);
        anotherAd.setAuthor(anotherUser);
        entityManager.persistAndFlush(anotherAd);

        // When - ищем все объявления первого пользователя
        List<AdEntity> userAds = adRepository.findAllByAuthor_Id(testUser.getId());

        // Then - проверяем, что найдены только объявления первого пользователя
        assertFalse(userAds.isEmpty(), "Должны быть найдены объявления пользователя");
        assertEquals(1, userAds.size(), "Должно быть найдено ровно одно объявление пользователя");
        assertEquals(testAd.getId(), userAds.get(0).getId(), "ID объявления должно совпадать");
        assertTrue(userAds.stream().allMatch(ad -> ad.getAuthor().getId().equals(testUser.getId())),
                "Все найденные объявления должны принадлежать указанному автору");
    }

    /**
     * Тест проверяет поиск объявления по ID и автору (комбинированный поиск)
     */
    @Test
    void findByIdAndAuthorId_ShouldReturnAdWhenExists() {
        // When - ищем объявление по ID и автору
        Optional<AdEntity> foundAd = adRepository.findByIdAndAuthor_Id(testAd.getId(), testUser.getId());

        // Then - проверяем, что объявление найдено
        assertTrue(foundAd.isPresent(), "Объявление должно быть найдено по ID и автору");
        assertEquals(testAd.getId(), foundAd.get().getId(), "ID объявления должно совпадать");
        assertEquals(testUser.getId(), foundAd.get().getAuthor().getId(), "ID автора должно совпадать");
    }

    /**
     * Тест проверяет, что метод findByIdAndAuthorId возвращает пустой результат при несовпадении автора
     */
    @Test
    void findByIdAndAuthorId_ShouldReturnEmptyWhenAuthorNotMatch() {
        // Given - создаем второго пользователя
        UserEntity anotherUser = new UserEntity();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("Jane");
        anotherUser.setLastName("Smith");
        anotherUser.setPhone("+79997654321");
        anotherUser.setPassword("password");
        anotherUser.setRole(Role.USER);
        anotherUser = entityManager.persistAndFlush(anotherUser);

        // When - ищем объявление первого пользователя с ID второго пользователя
        Optional<AdEntity> foundAd = adRepository.findByIdAndAuthor_Id(testAd.getId(), anotherUser.getId());

        // Then - проверяем, что результат пустой
        assertFalse(foundAd.isPresent(), "Не должно находить объявление при несовпадении автора");
    }

    /**
     * Тест проверяет сохранение нового объявления
     */
    @Test
    void save_ShouldPersistNewAd() {
        // Given - новое объявление
        AdEntity newAd = new AdEntity();
        newAd.setTitle("New Ad");
        newAd.setDescription("New Description");
        newAd.setPrice(5000);
        newAd.setAuthor(testUser);

        // When - сохраняем объявление
        AdEntity savedAd = adRepository.save(newAd);

        // Then - проверяем, что объявление сохранено с ID
        assertNotNull(savedAd.getId(), "Сохраненное объявление должно иметь ID");
        assertEquals("New Ad", savedAd.getTitle(), "Заголовок должен сохраниться");
        assertEquals("New Description", savedAd.getDescription(), "Описание должно сохраниться");
        assertEquals(5000, savedAd.getPrice(), "Цена должна сохраниться");
        assertEquals(testUser.getId(), savedAd.getAuthor().getId(), "Автор должен сохраниться");
    }

    /**
     * Тест проверяет обновление существующего объявления
     */
    @Test
    void save_ShouldUpdateExistingAd() {
        // Given - обновляем данные существующего объявления
        testAd.setTitle("Updated Title");
        testAd.setDescription("Updated Description");
        testAd.setPrice(1500);

        // When - сохраняем изменения
        AdEntity updatedAd = adRepository.save(testAd);

        // Then - проверяем, что данные обновились
        assertEquals(testAd.getId(), updatedAd.getId(), "ID должен остаться прежним");
        assertEquals("Updated Title", updatedAd.getTitle(), "Заголовок должен обновиться");
        assertEquals("Updated Description", updatedAd.getDescription(), "Описание должно обновиться");
        assertEquals(1500, updatedAd.getPrice(), "Цена должна обновиться");
    }

    /**
     * Тест проверяет удаление объявления
     */
    @Test
    void deleteById_ShouldRemoveAd() {
        // Given - ID существующего объявления
        Integer adId = testAd.getId();

        // When - удаляем объявление
        adRepository.deleteById(adId);
        entityManager.flush();

        // Then - проверяем, что объявление больше не существует
        Optional<AdEntity> foundAd = adRepository.findById(adId);
        assertFalse(foundAd.isPresent(), "Объявление должно быть удалено");
    }

    /**
     * Тест проверяет поиск несуществующего объявления
     */
    @Test
    void findById_ShouldReturnEmptyForNonExistentAd() {
        // When - ищем несуществующее объявление
        Optional<AdEntity> foundAd = adRepository.findById(999);

        // Then - проверяем, что результат пустой
        assertFalse(foundAd.isPresent(), "Не должно находить несуществующее объявление");
    }

    /**
     * Тест проверяет поиск всех объявлений
     */
    @Test
    void findAll_ShouldReturnAllAds() {
        // Given - создаем еще одно объявление
        AdEntity anotherAd = new AdEntity();
        anotherAd.setTitle("Second Ad");
        anotherAd.setDescription("Second Description");
        anotherAd.setPrice(3000);
        anotherAd.setAuthor(testUser);
        entityManager.persistAndFlush(anotherAd);

        // When - получаем все объявления
        List<AdEntity> allAds = adRepository.findAll();

        // Then - проверяем, что найдены все объявления
        assertEquals(2, allAds.size(), "Должны быть найдены все объявления");
        assertTrue(allAds.stream().anyMatch(ad -> ad.getId().equals(testAd.getId())),
                "Должно содержать первое объявление");
        assertTrue(allAds.stream().anyMatch(ad -> ad.getId().equals(anotherAd.getId())),
                "Должно содержать второе объявление");
    }

    /**
     * Тест проверяет, что связь с автором сохраняется правильно
     */
    @Test
    void adShouldMaintainRelationshipWithAuthor() {
        // When - загружаем объявление с автором
        Optional<AdEntity> foundAd = adRepository.findById(testAd.getId());

        // Then - проверяем, что связь с автором сохраняется
        assertTrue(foundAd.isPresent(), "Объявление должно быть найдено");
        assertNotNull(foundAd.get().getAuthor(), "Автор должен быть установлен");
        assertEquals(testUser.getId(), foundAd.get().getAuthor().getId(), "ID автора должно совпадать");
        assertEquals(testUser.getEmail(), foundAd.get().getAuthor().getEmail(), "Email автора должен совпадать");
        assertEquals(testUser.getFirstName(), foundAd.get().getAuthor().getFirstName(), "Имя автора должно совпадать");
        assertEquals(testUser.getLastName(), foundAd.get().getAuthor().getLastName(), "Фамилия автора должна совпадать");
    }

    /**
     * Тест проверяет работу при отсутствии объявлений у пользователя
     */
    @Test
    void findAllByAuthorId_ShouldReturnEmptyListForUserWithoutAds() {
        // Given - создаем пользователя без объявлений
        UserEntity userWithoutAds = new UserEntity();
        userWithoutAds.setEmail("noads@example.com");
        userWithoutAds.setFirstName("No");
        userWithoutAds.setLastName("Ads");
        userWithoutAds.setPhone("+79990000000");
        userWithoutAds.setPassword("password");
        userWithoutAds.setRole(Role.USER);
        userWithoutAds = entityManager.persistAndFlush(userWithoutAds);

        // When - ищем объявления пользователя без объявлений
        List<AdEntity> userAds = adRepository.findAllByAuthor_Id(userWithoutAds.getId());

        // Then - проверяем, что список пустой
        assertTrue(userAds.isEmpty(), "Для пользователя без объявлений должен возвращаться пустой список");
    }

    /**
     * Тест проверяет работу с несколькими объявлениями одного автора
     */
    @Test
    void findAllByAuthorId_ShouldReturnMultipleAdsForSameAuthor() {
        // Given - создаем еще два объявления для того же автора
        AdEntity secondAd = new AdEntity();
        secondAd.setTitle("Second Ad");
        secondAd.setDescription("Second Description");
        secondAd.setPrice(2000);
        secondAd.setAuthor(testUser);
        entityManager.persistAndFlush(secondAd);

        AdEntity thirdAd = new AdEntity();
        thirdAd.setTitle("Third Ad");
        thirdAd.setDescription("Third Description");
        thirdAd.setPrice(3000);
        thirdAd.setAuthor(testUser);
        entityManager.persistAndFlush(thirdAd);

        // When - ищем все объявления автора
        List<AdEntity> userAds = adRepository.findAllByAuthor_Id(testUser.getId());

        // Then - проверяем, что найдены все три объявления
        assertEquals(3, userAds.size(), "Должны быть найдены все три объявления автора");
        assertTrue(userAds.stream().allMatch(ad -> ad.getAuthor().getId().equals(testUser.getId())),
                "Все найденные объявления должны принадлежать указанному автору");

        // Проверяем, что все объявления имеют разные ID
        long distinctIds = userAds.stream().map(AdEntity::getId).distinct().count();
        assertEquals(3, distinctIds, "Все объявления должны иметь уникальные ID");
    }

}
