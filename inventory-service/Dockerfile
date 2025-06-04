FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY ../proto-common/pom.xml /proto-common/
COPY ../proto-common/src /proto-common/src
RUN cd /proto-common && mvn clean install

COPY inventory-service/pom.xml /app/pom.xml
COPY inventory-service/src /app/src
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
