FROM openjdk:17-jdk-alpine
ADD ${project.build.finalName}.jar ${project.build.finalName}.jar
ENTRYPOINT ["java", "-jar", "${project.build.finalName}.jar"]