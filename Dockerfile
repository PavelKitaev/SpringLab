FROM gradle:8-jdk17 as build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
