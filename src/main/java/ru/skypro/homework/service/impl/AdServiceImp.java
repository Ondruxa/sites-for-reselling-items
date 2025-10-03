package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.ImageService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с объявлениями (AdService).
 * <p>
 * Основные функции:
 * <ul>
 *   <li>Создание, обновление, удаление объявлений</li>
 *   <li>Работа с изображениями объявлений</li>
 *   <li>Получение объявлений и комментариев</li>
 *   <li>Проверка прав пользователя на действия</li>
 * </ul>
 * <p>
 * Исключения:
 * <ul>
 *   <li>IllegalArgumentException — если данные некорректны или отсутствуют</li>
 *   <li>AccessDeniedException — если недостаточно прав</li>
 *   <li>IllegalStateException — если не найден текущий пользователь</li>
 * </ul>
 */
@Slf4j
@Service
public class AdServiceImp implements AdService {

    private final AdRepository adRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AdMapper adMapper;
    private final CommentMapper commentMapper;
    private final ImageService imageService;

    public AdServiceImp(AdRepository adRepository,
                        CommentRepository commentRepository,
                        UserRepository userRepository,
                        AdMapper adMapper,
                        CommentMapper commentMapper,
                        ImageService imageService) {
        this.adRepository = adRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.adMapper = adMapper;
        this.commentMapper = commentMapper;
        this.imageService = imageService;
    }

    /**
     * Создаёт новое объявление и сохраняет изображение, если оно передано.
     * @param properties DTO с данными объявления
     * @param image изображение объявления (может быть null)
     * @throws IllegalStateException если не найден текущий пользователь
     * @throws IllegalArgumentException если properties отсутствует
     */
    @Override
    public void addAd(CreateOrUpdateAd properties, MultipartFile image) {
        UserEntity author = getCurrentUser();
        if (author == null) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }
        if (properties == null) {
            throw new IllegalArgumentException("Данные объявления отсутствуют");
        }
        AdEntity entity = adMapper.fromCreate(properties, author);
        adRepository.save(entity);
        if (image != null && !image.isEmpty()) {
            ImageEntity saved = imageService.save(image, "ad_" + entity.getId());
            entity.setImage(saved);
            adRepository.save(entity);
        }
    }

    /**
     * Возвращает все объявления.
     * @return DTO Ads с коллекцией объявлений
     */
    @Override
    @Transactional(readOnly = true)
    public Ads getAllAds() {
        List<Ad> list = adRepository.findAll().stream()
                .map(adMapper::toDto)
                .collect(Collectors.toList());
        Ads wrapper = new Ads();
        wrapper.setResults(list);
        wrapper.setCount(list.size());
        return wrapper;
    }

    /**
     * Возвращает расширенную информацию по объявлению по id.
     * @param id идентификатор объявления
     * @return DTO ExtendedAd
     * @throws IllegalArgumentException если объявление не найдено
     */
    @Override
    @Transactional(readOnly = true)
    public ExtendedAd getAdById(Integer id) {
        AdEntity entity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        return adMapper.toExtendedDto(entity);
    }

    /**
     * Удаляет объявление по id, удаляет связанное изображение.
     * @param id идентификатор объявления
     * @throws IllegalArgumentException если объявление не найдено
     * @throws AccessDeniedException если недостаточно прав
     */
    @Override
    public void removeAd(Integer id) {
        AdEntity entity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        UserEntity current = getCurrentUser();
        if (!canModify(entity, current)) {
            throw new AccessDeniedException("Недостаточно прав для удаления объявления");
        }
        if (entity.getImage() != null) {
            imageService.delete(entity.getImage().getId());
        }
        adRepository.delete(entity);
    }

    /**
     * Обновляет объявление по id.
     * @param updatedData DTO с новыми данными
     * @param id идентификатор объявления
     * @return DTO Ad
     * @throws IllegalArgumentException если нет данных или объявление не найдено
     * @throws AccessDeniedException если недостаточно прав
     */
    @Override
    @Transactional
    public Ad updateAd(CreateOrUpdateAd updatedData, Integer id) {
        if (updatedData == null) {
            throw new IllegalArgumentException("Нет данных для обновления");
        }
        AdEntity entity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        UserEntity current = getCurrentUser();
        if (!canModify(entity, current)) {
            throw new AccessDeniedException("Недостаточно прав для обновления объявления");
        }
        log.debug("Обновление объявления id={} новым title='{}', price={}, desc length={}", id, updatedData.getTitle(), updatedData.getPrice(), updatedData.getDescription() != null ? updatedData.getDescription().length() : 0);
        adMapper.updateEntity(updatedData, entity);
        adRepository.save(entity);
        Ad dto = adMapper.toDto(entity);
        if (dto.getImage() == null) {
            dto.setImage("");
        }
        return dto;
    }

    /**
     * Возвращает объявления текущего пользователя.
     * @return DTO Ads
     * @throws IllegalStateException если пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public Ads getUserAds() {
        UserEntity current = getCurrentUser();
        if (current == null) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }
        List<Ad> ads = adRepository.findAllByAuthor_Id(current.getId()).stream()
                .map(adMapper::toDto)
                .collect(Collectors.toList());
        Ads wrapper = new Ads();
        wrapper.setResults(ads);
        wrapper.setCount(ads.size());
        return wrapper;
    }

    /**
     * Обновляет изображение объявления.
     * @param id идентификатор объявления
     * @param file новое изображение
     * @return DTO Ad
     * @throws IllegalArgumentException если объявление не найдено или файл пустой
     * @throws AccessDeniedException если недостаточно прав
     */
    @Override
    public Ad updateImage(Integer id, MultipartFile file) {
        AdEntity entity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        UserEntity current = getCurrentUser();
        if (!canModify(entity, current)) {
            throw new AccessDeniedException("Недостаточно прав для обновления изображения объявления");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения пустой");
        }
        ImageEntity oldImage = entity.getImage();
        ImageEntity newImage = imageService.save(file, "ad_" + entity.getId());
        entity.setImage(newImage);
        adRepository.save(entity);
        if (oldImage != null) {
            imageService.delete(oldImage.getId());
        }
        return adMapper.toDto(entity);
    }

    /**
     * Возвращает комментарии к объявлению.
     * @param adId идентификатор объявления
     * @return DTO Comments
     * @throws IllegalArgumentException если объявление не найдено
     */
    @Override
    @Transactional(readOnly = true)
    public Comments getAdComments(Integer adId) {
        adRepository.findById(adId).orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        List<CommentEntity> list = commentRepository.findAllByAd_Id(adId);
        return commentMapper.toDtos(list);
    }

    /**
     * Добавляет комментарий к объявлению.
     * @param commentData DTO с текстом комментария
     * @param adId идентификатор объявления
     * @return DTO Comment
     * @throws IllegalArgumentException если нет данных или объявление не найдено
     * @throws IllegalStateException если пользователь не найден
     */
    @Override
    public Comment addComment(CreateOrUpdateComment commentData, Integer adId) {
        if (commentData == null) {
            throw new IllegalArgumentException("Нет данных комментария");
        }
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        UserEntity author = getCurrentUser();
        if (author == null) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }
        CommentEntity entity = commentMapper.fromCreate(commentData, ad, author);
        commentRepository.save(entity);
        return commentMapper.toDto(entity);
    }

    /**
     * Обновляет комментарий к объявлению.
     * @param updatedData DTO с новым текстом
     * @param adId идентификатор объявления
     * @param commentId идентификатор комментария
     * @return DTO Comment
     * @throws IllegalArgumentException если нет данных или комментарий не найден
     * @throws AccessDeniedException если недостаточно прав
     */
    @Override
    public Comment updateComment(CreateOrUpdateComment updatedData, Integer adId, Integer commentId) {
        if (updatedData == null) {
            throw new IllegalArgumentException("Нет данных для обновления комментария");
        }
        CommentEntity entity = commentRepository.findByIdAndAd_Id(commentId, adId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        UserEntity current = getCurrentUser();
        if (!canModify(entity.getAuthor(), current)) {
            throw new AccessDeniedException("Недостаточно прав для обновления комментария");
        }
        commentMapper.updateEntity(updatedData, entity);
        commentRepository.save(entity);
        return commentMapper.toDto(entity);
    }

    /**
     * Удаляет комментарий к объявлению.
     * @param adId идентификатор объявления
     * @param commentId идентификатор комментария
     * @throws IllegalArgumentException если комментарий не найден
     * @throws AccessDeniedException если недостаточно прав
     */
    @Override
    public void deleteComment(Integer adId, Integer commentId) {
        CommentEntity entity = commentRepository.findByIdAndAd_Id(commentId, adId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        UserEntity current = getCurrentUser();
        if (!canModify(entity.getAuthor(), current)) {
            throw new AccessDeniedException("Недостаточно прав для удаления комментария");
        }
        commentRepository.delete(entity);
    }

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        Optional<UserEntity> user = userRepository.findByEmail(auth.getName());
        return user.orElse(null);
    }

    private boolean canModify(AdEntity ad, UserEntity current) {
        if (ad == null || current == null) return false;
        return ad.getAuthor().getId().equals(current.getId()) || current.getRole() == Role.ADMIN;
    }

    private boolean canModify(UserEntity author, UserEntity current) {
        if (author == null || current == null) return false;
        return author.getId().equals(current.getId()) || current.getRole() == Role.ADMIN;
    }
}
