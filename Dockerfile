# Этап 1 — сборка
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Скопировать pom.xml и зависимости заранее (кэширование)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Скопировать исходники и собрать jar
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2 — рантайм
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем собранный jar из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Секрет jwt_private_key будет примонтирован Docker Compose автоматически
# и доступен в /run/secrets/jwt_private_key

# Порт приложения
EXPOSE 8080

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]
