FROM openjdk:17

COPY ../build/libs/*-SNAPSHOT.jar app.jar

ARG ENVIRONMENT

ENV SPRIING_PROFIlES_ACTIVE=${ENVIRONMENT}

ENV TZ Asia/Seoul

ENTRYPOINT ["java","-jar","/app.jar"]
