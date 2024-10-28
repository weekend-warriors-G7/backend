# Use a base image with Java
FROM openjdk:23-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

# Expose the Spring Boot port (8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
