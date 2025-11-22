package com.github.klijjen.urlshortener.repository;

import com.github.klijjen.urlshortener.Utils;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "urls", indexes = {
        @Index(name = "idx_short_code", columnList = "shortCode"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class UrlEntity {

    @Id
    private final UUID id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    public UrlEntity() {
        this.id = Utils.getRandomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public UrlEntity(String originalUrl, String shortCode) {
        this();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}