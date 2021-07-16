FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine

RUN mkdir /app
COPY ./target/feedly-reader-standalone.jar /app
RUN cd /app

# RUN mkdir target
# COPY target/feedly-reader-standalone.jar target

WORKDIR /app

CMD java -jar feedly-reader-standalone.jar
