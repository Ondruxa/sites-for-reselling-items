package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.CreateOrUpdateAd;

public interface AdService {
    void addAd( CreateOrUpdateAd properties, MultipartFile image);
}
