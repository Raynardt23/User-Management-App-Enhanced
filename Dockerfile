# Stage 1: Build the WAR using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Package the WAR
RUN mvn package -DskipTests

# Stage 2: Run the WAR with Tomcat (or Jetty)
FROM tomcat:10.1-jdk17

# Remove default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy WAR from build stage
COPY --from=build /app/target/UserManagementApp.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
