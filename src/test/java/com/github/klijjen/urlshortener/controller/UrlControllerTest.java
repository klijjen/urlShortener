package com.github.klijjen.urlshortener.controller;

import com.github.klijjen.urlshortener.controller.dto.ShortenRequest;
import com.github.klijjen.urlshortener.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Тесты REST контроллера для сокращения URL")
@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlService urlService;


    @Test
    @DisplayName("Должен вернуть короткий URL при валидном оригинальном URL")
    void shortenUrl_WithValidUrl_ShouldReturnShortUrl() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest("https://example.com", null);
        String expectedShortUrl = "http://localhost:8080/abc123";

        when(urlService.shortenUrl(anyString())).thenReturn(expectedShortUrl);

        // Act & Assert
        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.shortUrl").value(expectedShortUrl)).andExpect(jsonPath("$.originalUrl").value("https://example.com"));
    }

    @Test
    @DisplayName("Должен вернуть короткий URL при валидном URL и указанной длине")
    void shortenUrl_WithValidUrlAndLength_ShouldReturnShortUrl() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest("https://example.com", 8);
        String expectedShortUrl = "http://localhost:8080/abc12345";

        when(urlService.shortenUrl(anyString(), anyInt())).thenReturn(expectedShortUrl);

        // Act & Assert
        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.shortUrl").value(expectedShortUrl));
    }

    @Test
    @DisplayName("Должен вернуть 400 при невалидном URL")
    void shortenUrl_WithInvalidUrl_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest("invalid-url", null);

        // Act & Assert
        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть 400 при пустом URL")
    void shortenUrl_WithEmptyUrl_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest("", null);

        // Act & Assert
        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть 400 при слишком длинном URL")
    void shortenUrl_WithTooLongUrl_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String longUrl = "https://example.com/" + "a".repeat(3000);
        ShortenRequest request = new ShortenRequest(longUrl, null);

        // Act & Assert
        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть 400 при ошибке сервиса")
    void shortenUrl_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest("https://example.com", null);

        when(urlService.shortenUrl(anyString())).thenThrow(new IllegalArgumentException("Invalid URL format"));

        // Act & Assert
        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Ошибка валидации")).andExpect(jsonPath("$.message").value("Invalid URL format"));
    }


    @Test
    @DisplayName("Должен выполнить редирект при валидном коротком коде")
    void redirectToOriginalUrl_WithValidShortCode_ShouldRedirect() throws Exception {
        // Arrange
        String shortCode = "abc123";
        String originalUrl = "https://example.com";

        when(urlService.getOriginalUrl(shortCode)).thenReturn(Optional.of(originalUrl));

        // Act & Assert
        mockMvc.perform(get("/{shortCode}", shortCode)).andExpect(status().isFound()).andExpect(header().string("Location", originalUrl));
    }

    @Test
    @DisplayName("Должен вернуть 404 при невалидном коротком коде")
    void redirectToOriginalUrl_WithInvalidShortCode_ShouldReturnNotFound() throws Exception {
        // Arrange
        String shortCode = "invalid";

        when(urlService.getOriginalUrl(shortCode)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/{shortCode}", shortCode)).andExpect(status().isNotFound());
    }
}