FROM openjdk:17

COPY ./build/libs/*-SNAPSHOT.jar app.jar

ARG ENVIRONMENT

ENV SPRIING_PROFIlES_ACTIVE=${ENVIRONMENT}

ENV TZ Asia/Seoul
ENV JAVA_OPTS="-Xms6g -Xmx6g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled -XX:+UnlockExperimentalVMOptions -XX:G1HeapRegionSize=16m -XX:InitiatingHeapOccupancyPercent=45 -XX:ParallelGCThreads=4 -XX:ConcGCThreads=2"

ENTRYPOINT java $JAVA_OPTS -jar /app.jar
