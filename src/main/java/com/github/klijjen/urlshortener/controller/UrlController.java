package com.github.klijjen.urlshortener.controller;

import com.github.klijjen.urlshortener.controller.dto.ShortenRequest;
import com.github.klijjen.urlshortener.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class UrlController {
    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);
    private final UrlService urlService;

    @Autowired
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, String> response = new HashMap<>();
        response.put("error", "Ошибка валидации");
        response.put("message", errors.toString());

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        logger.info("Получен запрос на сокращение URL: {}", request.getUrl());

        try {
            String shortUrl;

            if (request.getLength() != null) {
                shortUrl = urlService.shortenUrl(request.getUrl(), request.getLength());
            } else {
                shortUrl = urlService.shortenUrl(request.getUrl());
            }

            Map<String, String> response = new HashMap<>();
            response.put("shortUrl", shortUrl);
            response.put("originalUrl", request.getUrl());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка валидации");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {
        logger.debug("Запрос редиректа для короткого кода: {}", shortCode);

        Optional<String> originalUrl = urlService.getOriginalUrl(shortCode);

        if (originalUrl.isPresent()) {
            logger.info("Редирект с {} на {}", shortCode, originalUrl.get());
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", originalUrl.get()).build();
        } else {
            logger.warn("URL не найден для короткого кода: {}", shortCode);
            return ResponseEntity.notFound().build();
        }
    }

}
