#
# Build stage
#
FROM maven:3.6.1-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jdk-slim
COPY --from=build /home/app/target/user-management-1.0.0-SNAPSHOT-fat.jar /app/app.jar
CMD java $JAVA_OPTS -jar /app/app.jar

#FROM openjdk:11-jdk-slim
#COPY target/user-management-1.0.0-SNAPSHOT-fat.jar /app/app.jar
#CMD java $JAVA_OPTS -jar /app/app.jar
