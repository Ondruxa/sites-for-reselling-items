package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.service.AdService;

@Service
public class AdServiceImp implements AdService {
    @Override
    public void addAd(CreateOrUpdateAd properties, MultipartFile image) {
    }
}
