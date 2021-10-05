FROM gradle AS build-stage
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

FROM alpine as production-stage

RUN apk update && apk add bash && apk --no-cache add openjdk11

RUN mkdir /app
COPY --from=build-stage /home/gradle/src/build/libs/*.jar /app
COPY src/main/resources/application.yml /app
RUN sed -i -r "s/localhost/boson-db/g" /app/application.yml
COPY ./wait-for-it.sh /app
