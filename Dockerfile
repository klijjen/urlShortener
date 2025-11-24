FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# Копируем только нужные файлы для кэширования зависимостей
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Скачиваем зависимости (кешируется отдельно)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Сборка приложения
RUN ./mvnw clean package -DskipTests

# Запуск
ENTRYPOINT ["java", "-jar", "/app/target/urlshortener-1.0.0.jar"]