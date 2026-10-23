# Imagen genérica para cualquiera de los microservicios.
# Uso (lo hace docker-compose):  docker build --build-arg SERVICE=user-service -t user-service .
FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /build
COPY ${SERVICE}/pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY ${SERVICE}/src ./src
RUN mvn -q -B package -DskipTests && cp target/*.jar /build/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
