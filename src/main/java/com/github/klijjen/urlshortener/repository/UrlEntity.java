package com.github.klijjen.urlshortener.repository;

import jakarta.persistence.*;

import java.util.*;

import static com.github.klijjen.urlshortener.Utils.getRandomUUID;


@Entity
@Table(name = "urls", indexes = {@Index(name = "idx_short_code", columnList = "shortCode"),})
public class UrlEntity {

    @Id
    private final UUID id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    public UrlEntity() {
        this.id = getRandomUUID();
    }

    public UrlEntity(String originalUrl, String shortCode) {
        this();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }
}