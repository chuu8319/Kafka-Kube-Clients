FROM eclipse-temurin:17-jre
COPY app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]