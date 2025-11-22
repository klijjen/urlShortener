# URL Shortener Service

Сервис для сокращения ссылок

## Установка и запуск


```bash
#Клонирование репозитория
git clone https://github.com/klijjen/urlShortener.git
cd urlShortener
````

```bash
# Сборка и запуск
./mvnw spring-boot:run
```

```bash
#Запуск тестов
./mvnw test
```



## Создание короткой ссылки

Создает короткую ссылку для указанного URL

**Endpoint:** `POST /shorten`

**Тело запроса:**
```json
{
  "url": "https://example.com/very/long/url",
  "length": 8
}
```

**Параметры:**
- `url` (обязательный) - оригинальный URL для сокращения
- `length` (опциональный) - желаемая длина короткого кода (по умолчанию 6)

**Успешный ответ (201 Created):**
```json
{
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://example.com/very/long/url"
}
```

**Ошибки:**
- `400 Bad Request` - невалидный URL или некорректная длина
- `500 Internal Server Error` - внутренняя ошибка сервера

**Пример использования:**
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://github.com/klijjen/urlShortener", "length": 8}'
```

## Переход по короткой ссылке

Перенаправляет на оригинальный URL по короткому коду

**Endpoint:** `GET /{shortCode}`

**Параметры:**
- `shortCode` - короткий код ссылки

**Ответы:**
- `302 Found` - успешный редирект на оригинальный URL
- `404 Not Found` - короткий код не найден

**Пример использования:**
```bash
curl -I http://localhost:8080/6ddacd
```