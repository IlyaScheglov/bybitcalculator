FROM maven:3.8.5-openjdk-17-slim AS builder
RUN mkdir -p /root/.m2 \
    && mkdir /root/.m2/repository
COPY . /usr/src/
COPY pom.xml .
COPY settings.xml /root/.m2/
COPY settings.xml /usr/share/maven/ref/
WORKDIR /usr/src/
RUN mvn clean install -Dmaven.test.skip=true -s settings.xml

FROM openjdk:17-ea-oracle
WORKDIR /app
COPY --from=builder /usr/src/target/*.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]