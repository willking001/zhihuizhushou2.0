# Stage 1: Build the application with Maven
FROM maven:3.8-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:17-jre-slim

WORKDIR /app

RUN apt-get update && apt-get install -y iputils-ping telnet curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# 使用 java -jar 命令来启动 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]