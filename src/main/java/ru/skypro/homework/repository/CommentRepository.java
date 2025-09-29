package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.CommentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    List<CommentEntity> findAllByAd_Id(Integer adId);
    Optional<CommentEntity> findByIdAndAd_Id(Integer id, Integer adId);
    void deleteByIdAndAd_Id(Integer id, Integer adId);
}

