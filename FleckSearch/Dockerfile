FROM maven:3.6.3-jdk-8 as build
COPY . /usr/src/FleckSearch
WORKDIR /usr/src/FleckSearch
RUN mvn clean compile package

FROM openjdk:8-jdk-buster
COPY --from=build /usr/src/FleckSearch .
RUN apt update
RUN apt install -y libxtst6 libxrender1

ENV AWT_TOOLKIT=MToolkit
ENV GOOGLE_APPLICATION_CREDENTIALS="/[key-file].json"

WORKDIR ./target
CMD ["java", "-jar", "FleckSearch-1-jar-with-dependencies.jar"]
