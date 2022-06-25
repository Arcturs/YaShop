FROM maven:3.8.3-openjdk-17-slim as BUILDER
ARG VERSION=0.0.1-SNAPSHOT
WORKDIR /opt/build/
COPY pom.xml /opt/build/
COPY src /opt/build/src/
RUN mvn -f /opt/build/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /opt/app/
EXPOSE 8080-8082
COPY --from=BUILDER /opt/build/target/*.jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]

