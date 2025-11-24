FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

ENTRYPOINT ["java", "-jar", "/app/target/urlshortener-1.0.0.jar"]