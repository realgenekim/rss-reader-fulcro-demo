FROM clojure:openjdk-11-tools-deps AS build

# FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
# FROM clojure:openjdk-11-lein-2.9.5
# FROM clojure:openjdk-11-tools-deps

#
# ================= IMAGE: BUILD
#

RUN apt-get update && apt-get install -y \
        make curl rlwrap time

WORKDIR /tmp
RUN curl -sL https://deb.nodesource.com/setup_14.x -o nodesource_setup.sh
RUN bash nodesource_setup.sh


RUN apt-get install -y nodejs

RUN node --version
RUN npm --version

RUN npm install --global yarn

WORKDIR /tmp
RUN curl -O https://download.clojure.org/install/linux-install-1.10.3.814.sh
RUN chmod +x linux-install-1.10.3.814.sh
RUN ./linux-install-1.10.3.814.sh

#
# cache dependencies
#

RUN mkdir /tmp/src
COPY package.json /tmp/src
COPY deps.edn /tmp/src
WORKDIR /tmp/src
RUN ls -lR
RUN yarn
RUN npm install -g shadow-cljs
RUN clj -A:dev:datomic:uberjar -P

# 
# build
#

RUN mkdir /src
# COPY ./target/feedly-reader-standalone.jar /app

COPY . /src
WORKDIR /src
RUN pwd
RUN ls -lR

# RUN npm install
# RUN npm install react react-dom create-react-class
RUN npm install react@17.0.2 react-dom@17.0.2 create-react-class@15.7.0

RUN make cljs-build-prod
RUN make uberjar


# RUN mkdir target
# COPY target/feedly-reader-standalone.jar target

#
# ================= IMAGE: RUN
#

FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine AS run

WORKDIR /
RUN mkdir /app
# COPY ./target/feedly-reader-standalone.jar /app

WORKDIR /app
COPY --from=build /src/target/feedly-reader-standalone.jar /app

RUN ls -lR

CMD java -jar feedly-reader-standalone.jar
