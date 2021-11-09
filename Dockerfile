FROM openjdk:16.0.2
COPY componentsToCopy /home
WORKDIR /home
CMD ["java", "-jar", "TeamManagerDiscordBot.jar", "-Xmx900"]