package com.example.urlshortener.controller.dto;

import jakarta.validation.constraints.*;

public class ShortenRequest {
    @NotBlank(message = "URL обязателен")
    @Size(max = 2048, message = "URL не должен превышать 2048 символов")
    @Pattern(regexp = "^(https?|ftp)://.*", message = "Неверный формат URL. Должен начинаться с http://, https:// или ftp://")
    private String url;

    @Min(value = 4, message = "Длина короткой ссылки должна быть не менее 4 символов")
    @Max(value = 12, message = "Длина короткой ссылки не может превышать 12 символа")
    private Integer length;

    public ShortenRequest() {
    }

    public ShortenRequest(String url, Integer length) {
        this.url = url;
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}