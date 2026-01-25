FROM eclipse-temurin:21-jdk-alpine
ADD ${project.build.finalName}.jar ${project.build.finalName}.jar
ENTRYPOINT ["java", "-jar", "${project.build.finalName}.jar"]