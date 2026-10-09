# ============================================================
# CourtHub · Dockerfile de la aplicación (Java 21 + Spring Boot MVC)
# Build multi-etapa: Maven compila el JAR → imagen final solo con JRE
# ============================================================

# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Descargar dependencias primero (mejor cacheo de capas)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Compilar y empaquetar (sin tests para acelerar el build)
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: imagen de ejecución ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/courthub-api-*.jar app.jar

ENV TZ=America/Lima
EXPOSE 8080

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
