FROM openjdk:8-jre-slim
COPY build/install/tele-ts3-bot/ /app/
ENV CONFIG_DIR=/config
RUN mkdir /config
VOLUME /config
CMD ["/app/bin/tele-ts3-bot"]
