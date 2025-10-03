package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;

/**
 * Сервис для управления объявлениями и их комментариями.
 * <p>
 * Основные зоны ответственности:
 * <ul>
 *   <li>CRUD операций над объявлениями.</li>
 *   <li>Работа с изображением объявления (замена / сохранение).</li>
 *   <li>CRUD операций над комментариями конкретного объявления.</li>
 *   <li>Получение списков (все объявления, объявления текущего пользователя) с агрегированным count.</li>
 * </ul>
 * </p>
 */
public interface AdService {
    /**
     * Создаёт новое объявление и сохраняет изображение.
     * @param properties DTO с основными данными
     * @param image файл изображения (может быть null)
     */
    void addAd(CreateOrUpdateAd properties, MultipartFile image);

    /**
     * Возвращает список всех объявлений в обёртке.
     * @return {@link Ads}
     */
    Ads getAllAds();

    /**
     * Получить расширенное описание объявления.
     * @param id идентификатор
     * @return {@link ExtendedAd}
     */
    ExtendedAd getAdById(Integer id);

    /**
     * Удалить объявление (включая связанное изображение / каскад комментариев).
     * @param id идентификатор
     */
    void removeAd(Integer id);

    /**
     * Обновить объявление.
     * @param updatedData новые данные
     * @param id идентификатор объявления
     * @return краткое DTO объявления
     */
    Ad updateAd(CreateOrUpdateAd updatedData, Integer id);

    /**
     * Объявления текущего пользователя.
     * @return {@link Ads}
     */
    Ads getUserAds();

    /**
     * Обновление изображения объявления.
     * @param id id объявления
     * @param file новое изображение
     * @return обновлённое DTO объявления
     */
    Ad updateImage(Integer id, MultipartFile file);

    /**
     * Получить все комментарии объявления.
     * @param adId id объявления
     * @return {@link Comments}
     */
    Comments getAdComments(Integer adId);

    /**
     * Добавить комментарий к объявлению.
     * @param commentData DTO текста
     * @param adId id объявления
     * @return созданный комментарий
     */
    Comment addComment(CreateOrUpdateComment commentData, Integer adId);

    /**
     * Обновить текст комментария.
     * @param updatedData новые данные
     * @param adId id объявления
     * @param commentId id комментария
     * @return обновлённый комментарий
     */
    Comment updateComment(CreateOrUpdateComment updatedData, Integer adId, Integer commentId);

    /**
     * Удалить комментарий.
     * @param adId id объявления
     * @param commentId id комментария
     */
    void deleteComment(Integer adId, Integer commentId);
}
