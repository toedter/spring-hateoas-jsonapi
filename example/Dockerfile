# Stage 1: Extract layers from pre-built JAR
FROM eclipse-temurin:25.0.3_9-jre-alpine AS builder

WORKDIR /workspace

# Copy the pre-built JAR
COPY build/libs/*.jar app.jar

# Extract layers (Spring Boot 4.x uses 'tools' jarmode instead of 'layertools')
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Stage 2: Runtime
FROM eclipse-temurin:25.0.3_9-jre-alpine

WORKDIR /app

# Copy layers in order (dependencies change less frequently than application code)
COPY --from=builder /workspace/extracted/dependencies/ ./
COPY --from=builder /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
