FROM maven:3.6.3-jdk-8-slim
COPY pom.xml .
COPY . .
WORKDIR .
RUN mvn clean install
CMD ["java", "-jar", "target/TeamManagerDiscordBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]
