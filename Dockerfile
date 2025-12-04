FROM gradle:8-jdk17 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN gradle build

FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
EXPOSE 8080
COPY --from=builder /builder/build/libs/SpringLab-1.0.jar .
CMD ["java", "-jar", "SpringLab-1.0.jar"]
