FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src src
RUN mvn -q -B package -DskipTests

FROM amazoncorretto:17.0.8
WORKDIR /app
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
