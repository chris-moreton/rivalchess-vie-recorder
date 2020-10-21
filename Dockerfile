FROM openjdk:8

VOLUME /tmp

ARG DEPENDENCY=build/libs

ARG JAR_FILE=build/libs/rivalchess-vie-recorder-1.0.0.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

