# Base image for building the spring-boot application (with Maven and JDK 21)
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY . /app
RUN mvn clean install -DskipTests

# Base image for building the stockfish (with GCC and Make)
FROM gcc:11 AS stockfish
RUN git clone https://github.com/official-stockfish/Stockfish.git /app/stockfish-repo
WORKDIR /app/stockfish-repo/src
RUN make -j build

# Final image for running the application (Alpine JDK 21)
FROM amazoncorretto:21-alpine3.21-jdk
WORKDIR /app
ENV ENGINE_PATH="/app/stockfish"
COPY --from=builder /app/target/*.jar /app/chess-app.jar
COPY --from=stockfish /app/stockfish-repo/src/stockfish /app/stockfish

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/chess-app.jar"]
