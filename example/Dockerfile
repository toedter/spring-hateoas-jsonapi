# Stage 1: Extract layers from pre-built JAR
FROM eclipse-temurin:21.0.10_7-jre-alpine AS builder

WORKDIR /workspace

# Copy the pre-built JAR
COPY build/libs/*.jar app.jar

# Extract layers
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 2: Runtime
FROM eclipse-temurin:21.0.10_7-jre-alpine

WORKDIR /app

# Copy layers in order (dependencies change less frequently than application code)
COPY --from=builder /workspace/dependencies/ ./
COPY --from=builder /workspace/spring-boot-loader/ ./
COPY --from=builder /workspace/snapshot-dependencies/ ./
COPY --from=builder /workspace/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
