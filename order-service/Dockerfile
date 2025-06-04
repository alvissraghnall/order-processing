FROM maven:3.8.6-openjdk-21 AS build
WORKDIR /app

COPY ../proto-common/pom.xml ../proto-common/
COPY ../proto-common/src ../proto-common/src
RUN cd ../proto-common && mvn clean install

COPY pom.xml .
COPY src src

RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
