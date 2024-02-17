### Builder
FROM ghcr.io/graalvm/native-image:ol8-java17-22 AS build

# Copy
WORKDIR /app
COPY pom.xml mvnw ./
COPY src ./src
COPY .mvn/ ./.mvn

# Build
RUN ./mvnw package -Pnative -DskipTests -Dspring-boot.run.profiles=prod

### Deployer
FROM amazonlinux:2023.3.20240131.0 AS deploy

# Copy
WORKDIR /app
COPY --from=build /app/target/aws-java-rest-api ./aws-java-rest-api

# User, port and health check
USER 1001
EXPOSE 8080
HEALTHCHECK CMD timeout 10s bash -c 'true > /dev/tcp/127.0.0.1/8080'

# Startup
ENTRYPOINT ["/app/aws-java-rest-api"]