FROM eclipse-temurin:21-jdk

# Указываем рабочую директорию внутри контейнера
WORKDIR /app

COPY target/spring-web-market-app-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт (если приложение слушает 8080)
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]