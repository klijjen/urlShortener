package com.example.urlshortener.repository;

import com.example.urlshortener.Utils;

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
    private UUID id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

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

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}