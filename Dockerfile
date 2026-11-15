# syntax=docker/dockerfile:1

# --- Construcción: compila con el mismo Java 21 de la CI (ADR-015) ---
FROM eclipse-temurin:21-jdk-noble AS construccion
WORKDIR /fuente
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Las dependencias van en su propia capa: si solo cambia el código, no se vuelven a descargar.
RUN sh mvnw -B -q dependency:go-offline
COPY src/ src/
RUN sh mvnw -B -q -DskipTests package

# --- Ejecución: solo el JRE y el jar, con un usuario sin privilegios ---
FROM eclipse-temurin:21-jre-alpine
ARG VERSION=local
LABEL org.opencontainers.image.source="https://github.com/jeromontoya19/uco-kudos-backend" \
      org.opencontainers.image.description="API de UCO Kudos" \
      org.opencontainers.image.revision="${VERSION}"
# La JVM usa como máximo el 75 % de la memoria del contenedor (réplica de 1 GB, ADR-015).
ENV APP_VERSION=${VERSION} \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
RUN addgroup -S ucokudos && adduser -S -G ucokudos ucokudos
WORKDIR /app
COPY --from=construccion /fuente/target/uco-kudos-api.jar app.jar
USER ucokudos
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
