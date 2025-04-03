FROM maven:3.9.6-eclipse-temurin-24 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:24-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080

EXPOSE ${PORT}

CMD ["java", "-jar", "app.jar", "--server.port=${PORT}"]
