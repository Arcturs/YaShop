FROM maven:3.8.3-openjdk-17-slim as BUILDER
WORKDIR /opt/build/
COPY pom.xml /opt/build/
COPY src /opt/build/src/

RUN mvn clean package -DskipTests
COPY target/*.jar /opt/build/target/

FROM openjdk:17-jdk-alpine
WORKDIR /opt/app/
COPY --from=BUILDER /opt/build/target/*.jar /opt/app/
EXPOSE 8080-8082
CMD java -jar /opt/app/*.jar

