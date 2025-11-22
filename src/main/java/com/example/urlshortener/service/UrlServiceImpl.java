package com.example.urlshortener.service;

import com.example.urlshortener.config.UrlShortenerConfig;
import com.example.urlshortener.repository.UrlEntity;
import com.example.urlshortener.repository.UrlRepository;
import org.apache.commons.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.urlshortener.Utils.getRandomUUID;

@Service
public class UrlServiceImpl implements UrlService {
    private final static Logger logger = LoggerFactory.getLogger(UrlServiceImpl.class);
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final int MAX_URL_LENGTH = 2048;

    private final UrlValidator urlValidator;
    private final UrlRepository urlRepository;
    private final UrlShortenerConfig config;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    public UrlServiceImpl(UrlRepository urlRepository, UrlShortenerConfig config) {
        this.urlRepository = urlRepository;
        this.config = config;
        this.urlValidator = new UrlValidator(new String[]{"http", "https", "ftp"});

        logger.info("Сервис сокращения URL сконфигурирован.");
    }

    @Override
    public String shortenUrl(String originalUrl) {
        return shortenUrl(originalUrl, config.getDefaultLength());
    }

    @Override
    public String shortenUrl(String originalUrl, int desiredLength) {
        logger.info("Сокращение URL: {} с желаемой длиной: {}", originalUrl, desiredLength);

        validateUrl(originalUrl);
        validateLength(desiredLength);

        Optional<UrlEntity> existingUrl = urlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            logger.debug("URL уже был сокращен ранее: {}", originalUrl);
            return buildShortUrl(existingUrl.get().getShortCode());
        }

        String shortCode = generateUniqueShortCode(desiredLength);
        UrlEntity urlEntity = new UrlEntity(originalUrl, shortCode);
        urlRepository.save(urlEntity);

        logger.info("URL успешно сокращен: {} -> {} (длина: {} символов)", originalUrl, shortCode, shortCode.length());
        return buildShortUrl(shortCode);
    }

    @Override
    public Optional<String> getOriginalUrl(String shortCode) {
        logger.debug("Поиск оригинального URL для короткого кода: {}", shortCode);

        return urlRepository.findByShortCode(shortCode)
                .map(UrlEntity::getOriginalUrl);
    }

    private String buildShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }

    private String generateUniqueShortCode(int desiredLength) {
        int attempts = 0;

        while (attempts < MAX_GENERATION_ATTEMPTS) {
            String uuid = getRandomUUID().toString().replace("-", "");
            String shortCode = uuid.substring(0, desiredLength);

            if (!urlRepository.existsByShortCode(shortCode)) {
                logger.debug("Уникальный короткий код успешно сгенерирован: {}", shortCode);
                return shortCode;
            }
            attempts++;
            logger.warn("Обнаружена коллизия короткого кода, попытка {}/{}", attempts, MAX_GENERATION_ATTEMPTS);
        }
        logger.error("Не удалось сгенерировать уникальный короткий код после {} попыток", MAX_GENERATION_ATTEMPTS);
        throw new IllegalStateException("Не удалось сгенерировать уникальный короткий код после " + MAX_GENERATION_ATTEMPTS + " попыток");
    }

    private void validateLength(int length) {
        if (length < config.getMinLength()) {
            throw new IllegalArgumentException("Длина короткого URL должна быть не менее " + config.getMinLength() + " символов");
        }

        if (length > config.getMaxLength()) {
            throw new IllegalArgumentException("Длина короткого URL не может превышать " + config.getMaxLength() + " символов");
        }

        logger.debug("Длина короткого URL проверена: {} символов", length);
    }

    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL не может быть пустым или null");
        }

        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("Длина URL превышает максимальный лимит в 2048 символов");
        }

        if (!urlValidator.isValid(url)) {
            throw new IllegalArgumentException("Неверный формат URL: " + url);
        }

        logger.debug("URL прошел валидацию: {}", url);
    }
}
