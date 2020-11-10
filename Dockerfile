#FROM maven:3.5.2-jdk-8-alpine
#FROM maven:3.6.3-jdk-8-slim
FROM maven:3.6.3-jdk-8
COPY pom.xml /
RUN mvn verify clean
COPY . /
WORKDIR /
RUN mvn package
CMD ["java", "-jar", "target/TeamManagerDiscordBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]
