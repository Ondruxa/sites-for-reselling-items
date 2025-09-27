package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdService;

import java.util.Collections;
import java.util.List;

@Service
public class AdServiceImp implements AdService {
    @Override
    public void addAd(CreateOrUpdateAd properties, MultipartFile image){}

    @Override
    public List<Ad> getAllAds() {
        // Имитация получения списка объявлений
        return Collections.emptyList();
    }

    @Override
    public ExtendedAd getAdById(Integer id) {
        // Возвращаем детальное объявление по его идентификатору
        return null;
    }

    @Override
    public void removeAd(Integer id) {
        // Удаляем объявление по указанному идентификатору
    }

    @Override
    public Ad updateAd(CreateOrUpdateAd updatedData, Integer id) {
        // Обновляем объявление по переданному идентификатору
        return null;
    }

    @Override
    public Ads getUserAds() {
        // Получаем объявления текущего авторизованного пользователя
        return null;
    }

    @Override
    public byte[] updateImage(Integer id, MultipartFile file) {
        // Обновляем изображение объявления
        return new byte[0];
    }

    @Override
    public Comments getAdComments(Integer adId) {

        return null;
    }

    @Override
    public Comment addComment(CreateOrUpdateComment commentData, Integer adId) {

        return null;
    }

    @Override
    public Comment updateComment(CreateOrUpdateComment updatedData, Integer adId, Integer commentId) {

        return null;
    }

    @Override
    public void deleteComment(Integer adId, Integer commentId) {

    }
}
