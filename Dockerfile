FROM openjdk:17.0.1
COPY componentsToCopy /home
WORKDIR /home
CMD ["java", "-jar", "TeamManagerDiscordBot.jar", "-Xmx900"]