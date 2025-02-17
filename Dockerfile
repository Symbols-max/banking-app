FROM openjdk:23

COPY target/banking-app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
