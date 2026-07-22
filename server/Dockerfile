# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src and build the application
COPY src ./src
RUN mvn package -DskipTests && rm -f target/*.original

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy executable jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
