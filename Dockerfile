<<<<<<< Updated upstream
FROM maven:3.6.3-jdk-8-slim
COPY pom.xml /tmp/
COPY teammanagerbotLocalizations.json /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn clean install
CMD ["java", "-jar", "target/TeamManagerDiscordBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]
=======
FROM openjdk:16.0.2
COPY componentsToCopy /home
WORKDIR /home
CMD ["java", "-jar", "TeamManagerDiscordBot.jar", "-Xmx900"]
>>>>>>> Stashed changes
