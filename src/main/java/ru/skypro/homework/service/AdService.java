package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;

import java.util.List;

public interface AdService {
    public void addAd( CreateOrUpdateAd properties, MultipartFile image);
    public List<Ad> getAllAds();
    public ExtendedAd getAdById(Integer id);
    public void removeAd(Integer id);
    public Ad updateAd(CreateOrUpdateAd updatedData, Integer id);
    public Ads getUserAds();
    public byte[] updateImage(Integer id, MultipartFile file);

}
