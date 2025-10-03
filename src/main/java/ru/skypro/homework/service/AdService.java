package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;

import java.util.List;

public interface AdService {
    void addAd(CreateOrUpdateAd properties, MultipartFile image);
    Ads getAllAds();
    ExtendedAd getAdById(Integer id);
    void removeAd(Integer id);
    Ad updateAd(CreateOrUpdateAd updatedData, Integer id);
    Ads getUserAds();
    Ad updateImage(Integer id, MultipartFile file);
    Comments getAdComments(Integer adId);
    Comment addComment(CreateOrUpdateComment commentData, Integer adId);
    Comment updateComment(CreateOrUpdateComment updatedData, Integer adId, Integer commentId);
    void deleteComment(Integer adId, Integer commentId);
}
