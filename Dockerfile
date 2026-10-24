FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S huertos && adduser -S huertos -G huertos && mkdir uploads && chown -R huertos:huertos /app
COPY --from=build /build/target/huertos-medicinales-0.1.0.jar app.jar
USER huertos
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
