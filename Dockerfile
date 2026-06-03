FROM eclipse-temurin:21-jre
COPY target/restaurant-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]