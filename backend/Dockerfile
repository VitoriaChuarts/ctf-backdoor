FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:21-jre
COPY --from=docker:27-cli /usr/local/bin/docker /usr/local/bin/docker
RUN useradd -m -u 10001 app
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
