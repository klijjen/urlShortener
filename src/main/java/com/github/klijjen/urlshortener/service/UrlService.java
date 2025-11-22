package com.github.klijjen.urlshortener.service;

import java.util.Optional;

public interface UrlService {
    String shortenUrl(String originalUrl);

    String shortenUrl(String originalUrl, int desiredLength);

    Optional<String> getOriginalUrl(String shortCode);
}
