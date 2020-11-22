FROM maven:3.6.3-jdk-8 as build
COPY . /usr/src/FleckSearch
WORKDIR /usr/src/FleckSearch
RUN mvn clean
RUN mvn compile
RUN mvn package

FROM openjdk:8-jdk-buster
COPY --from=build /usr/src/FleckSearch .
RUN apt update
RUN apt install -y libxtst6 libxrender1
WORKDIR ./target
CMD ["java", "-jar", "FleckSearch-1-jar-with-dependencies.jar"]
