FROM gradle:6.7-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist --no-daemon


FROM openjdk:8-jre-slim
COPY --from=build /home/gradle/src/build/install/TS3ClientNotify/ /app/
ENV CONFIG_DIR=/config
RUN mkdir /config
VOLUME /config
CMD ["/app/bin/TS3ClientNotify"]
