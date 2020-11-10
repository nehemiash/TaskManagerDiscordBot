#FROM maven:3.5.2-jdk-8-alpine
FROM maven:3.6.3-jdk-8-slim
COPY pom.xml /tmp/
RUN mvn verify clean
COPY . /tmp/
WORKDIR /tmp/
RUN mvn package
CMD ["java", "-jar", "target/TeamManagerDiscordBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]
