package com.example.urlshortener.service;

import com.example.urlshortener.config.UrlShortenerConfig;
import com.example.urlshortener.repository.UrlEntity;
import com.example.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование сервиса сокращения URL")
class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    private UrlServiceImpl urlService;

    private final String VALID_URL = "https://example.com";

    @BeforeEach
    void setUp() {
        UrlShortenerConfig config = new UrlShortenerConfig();
        config.setMinLength(4);
        config.setDefaultLength(6);
        config.setMaxLength(12);
        config.setMaxAttempts(10);

        urlService = new UrlServiceImpl(urlRepository, config);
    }

    @Test
    @DisplayName("Сокращение валидного URL должно возвращать короткую ссылку")
    void shortenUrl_WithValidUrl_ShouldReturnShortUrl() {
        // Arrange
        when(urlRepository.findByOriginalUrl(VALID_URL)).thenReturn(Optional.empty());
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = urlService.shortenUrl(VALID_URL);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("/"));
        String shortCode = result.substring(result.lastIndexOf("/") + 1);
        assertEquals(6, shortCode.length());

        verify(urlRepository).findByOriginalUrl(VALID_URL);
        verify(urlRepository).save(any(UrlEntity.class));
    }

    @Test
    @DisplayName("Сокращение уже существующего URL должно возвращать существующую короткую ссылку")
    void shortenUrl_WithExistingUrl_ShouldReturnExistingShortUrl() {
        // Arrange
        String existingShortCode = "abc123";
        UrlEntity existingEntity = new UrlEntity(VALID_URL, existingShortCode);

        when(urlRepository.findByOriginalUrl(VALID_URL)).thenReturn(Optional.of(existingEntity));

        // Act
        String result = urlService.shortenUrl(VALID_URL);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("/"));
        String shortCode = result.substring(result.lastIndexOf("/") + 1);
        assertEquals(existingShortCode, shortCode);

        verify(urlRepository).findByOriginalUrl(VALID_URL);
        verify(urlRepository, never()).save(any(UrlEntity.class));
    }

    @Test
    @DisplayName("Сокращение URL с пользовательской длиной должно возвращать ссылку указанной длины")
    void shortenUrl_WithCustomLength_ShouldReturnUrlWithSpecifiedLength() {
        // Arrange
        int customLength = 8;
        when(urlRepository.findByOriginalUrl(VALID_URL)).thenReturn(Optional.empty());
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = urlService.shortenUrl(VALID_URL, customLength);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("/"));
        String shortCode = result.substring(result.lastIndexOf("/") + 1);
        assertEquals(customLength, shortCode.length());
    }

    @Test
    @DisplayName("Сокращение null URL должно выбрасывать исключение")
    void shortenUrl_WithNullUrl_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> urlService.shortenUrl(null));

        assertEquals("URL не может быть пустым или null", exception.getMessage());
    }

    @Test
    @DisplayName("Сокращение пустого URL должно выбрасывать исключение")
    void shortenUrl_WithEmptyUrl_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> urlService.shortenUrl(""));

        assertEquals("URL не может быть пустым или null", exception.getMessage());
    }

    @Test
    @DisplayName("Сокращение невалидного URL должно выбрасывать исключение")
    void shortenUrl_WithInvalidUrl_ShouldThrowException() {
        // Arrange
        String invalidUrl = "not-a-valid-url";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> urlService.shortenUrl(invalidUrl));

        assertTrue(exception.getMessage().contains("Неверный формат URL"));
    }

    @Test
    @DisplayName("Сокращение слишком длинного URL должно выбрасывать исключение")
    void shortenUrl_WithTooLongUrl_ShouldThrowException() {
        // Arrange
        String longUrl = "https://example.com/" + "a".repeat(3000);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> urlService.shortenUrl(longUrl));

        assertEquals("Длина URL превышает максимальный лимит в 2048 символов", exception.getMessage());
    }

    @Test
    @DisplayName("Сокращение с слишком короткой длиной кода должно выбрасывать исключение")
    void shortenUrl_WithTooShortLength_ShouldThrowException() {
        // Arrange
        int tooShortLength = 2;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> urlService.shortenUrl(VALID_URL, tooShortLength));

        assertEquals("Длина короткого URL должна быть не менее 4 символов", exception.getMessage());
    }

    @Test
    @DisplayName("Сокращение с слишком длинной длиной кода должно выбрасывать исключение")
    void shortenUrl_WithTooLongLength_ShouldThrowException() {
        // Arrange
        int tooLongLength = 20;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> urlService.shortenUrl(VALID_URL, tooLongLength));

        assertEquals("Длина короткого URL не может превышать 12 символов", exception.getMessage());
    }

    @Test
    @DisplayName("Получение оригинального URL по существующему короткому коду должно возвращать URL")
    void getOriginalUrl_WithExistingShortCode_ShouldReturnOriginalUrl() {
        // Arrange
        String shortCode = "abc123";
        UrlEntity entity = new UrlEntity(VALID_URL, shortCode);

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(entity));

        // Act
        Optional<String> result = urlService.getOriginalUrl(shortCode);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(VALID_URL, result.get());
        verify(urlRepository).findByShortCode(shortCode);
    }

    @Test
    @DisplayName("Получение оригинального URL по несуществующему короткому коду должно возвращать пустой результат")
    void getOriginalUrl_WithNonExistingShortCode_ShouldReturnEmpty() {
        // Arrange
        String nonExistingCode = "nonexist";
        when(urlRepository.findByShortCode(nonExistingCode)).thenReturn(Optional.empty());

        // Act
        Optional<String> result = urlService.getOriginalUrl(nonExistingCode);

        // Assert
        assertFalse(result.isPresent());
        verify(urlRepository).findByShortCode(nonExistingCode);
    }

    @Test
    @DisplayName("Генерация уникального короткого кода при коллизиях должна генерировать уникальный код")
    void generateUniqueShortCode_WhenCollisionsOccur_ShouldGenerateUniqueCode() {
        // Arrange
        when(urlRepository.existsByShortCode(anyString())).thenReturn(true)  // Первые 2 попытки - коллизия
                .thenReturn(true).thenReturn(false); // Третья попытка - успех

        // Act
        String result = urlService.generateUniqueShortCode(6);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.length());
        verify(urlRepository, times(3)).existsByShortCode(anyString());
    }

    @Test
    @DisplayName("Генерация уникального короткого кода при превышении максимального количества попыток должна выбрасывать исключение")
    void generateUniqueShortCode_WhenMaxAttemptsExceeded_ShouldThrowException() {
        // Arrange
        when(urlRepository.existsByShortCode(anyString())).thenReturn(true); // Всегда коллизия

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> urlService.generateUniqueShortCode(6));

        assertTrue(exception.getMessage().contains("Не удалось сгенерировать уникальный короткий код после 10 попыток"));
        verify(urlRepository, times(10)).existsByShortCode(anyString());
    }

    @Test
    @DisplayName("Сокращение URL с минимально допустимой длиной кода должно работать корректно")
    void shortenUrl_WithMinAllowedLength_ShouldWork() {
        // Arrange
        int minLength = 4;
        when(urlRepository.findByOriginalUrl(VALID_URL)).thenReturn(Optional.empty());
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = urlService.shortenUrl(VALID_URL, minLength);

        // Assert
        String shortCode = result.substring(result.lastIndexOf("/") + 1);
        assertEquals(minLength, shortCode.length());
    }

    @Test
    @DisplayName("Сокращение URL с максимально допустимой длиной кода должно работать корректно")
    void shortenUrl_WithMaxAllowedLength_ShouldWork() {
        // Arrange
        int maxLength = 12;
        when(urlRepository.findByOriginalUrl(VALID_URL)).thenReturn(Optional.empty());
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = urlService.shortenUrl(VALID_URL, maxLength);

        // Assert
        String shortCode = result.substring(result.lastIndexOf("/") + 1);
        assertEquals(maxLength, shortCode.length());
    }

    @Test
    @DisplayName("Сокращение HTTP URL должно работать корректно")
    void shortenUrl_WithHttpUrl_ShouldWork() {
        // Arrange
        String httpUrl = "http://example.com";
        when(urlRepository.findByOriginalUrl(httpUrl)).thenReturn(Optional.empty());
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = urlService.shortenUrl(httpUrl);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("/"));
    }

    @Test
    @DisplayName("Сокращение FTP URL должно работать корректно")
    void shortenUrl_WithFtpUrl_ShouldWork() {
        // Arrange
        String ftpUrl = "ftp://example.com/file.txt";
        when(urlRepository.findByOriginalUrl(ftpUrl)).thenReturn(Optional.empty());
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = urlService.shortenUrl(ftpUrl);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("/"));
    }
}