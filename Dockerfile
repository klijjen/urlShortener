# Dockerfile
FROM eclipse-temurin:17-jdk-alpine

# Установка рабочей директории
WORKDIR /app

# Копирование pom.xml и скачивание зависимостей
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Скачивание зависимостей (кешируется отдельно)
RUN ./mvnw dependency:go-offline

# Копирование исходного кода
COPY src ./src

# Сборка приложения
RUN ./mvnw clean package -DskipTests

# Создание non-root пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Expose порт
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/target/urlshortener-0.0.1-SNAPSHOT.jar"]