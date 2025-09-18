# Use official Java 17 runtime
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the WAR file from the target folder
COPY target/UserManagementApp.war app.war

# Expose the port your app runs on
EXPOSE 8080

# Run the WAR file
CMD ["java", "-jar", "app.war"]
