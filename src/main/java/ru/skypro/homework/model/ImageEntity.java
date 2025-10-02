package ru.skypro.homework.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "images")
public class ImageEntity {
    @Id
    private String id; // UUID / file name

    @Column(name = "data")
    @Basic(fetch = FetchType.LAZY)
    private byte[] data; // null в fs режиме

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "checksum", length = 64)
    private String checksum; // SHA-256 хэш (hex)
}
