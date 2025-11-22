package com.example.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.short-url")
public class UrlShortenerConfig {

    private int minLength = 4;
    private int defaultLength = 6;
    private int maxLength = 12;

    public int getMinLength() {
        return minLength;
    }

    public int getDefaultLength() {
        return defaultLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public void setDefaultLength(int defaultLength) {
        this.defaultLength = defaultLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }
}