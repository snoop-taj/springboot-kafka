FROM maven:3.6.3-jdk-11-slim

RUN addgroup --gid 1000 maven && adduser --system -uid 1000 --gid 1000 maven && chown -R maven:maven /home/maven
USER maven

RUN mkdir -p /home/maven/.m2

WORKDIR /code
COPY pom.xml .
COPY src/ /code/src/

CMD ["mvn", "spring-boot:run"]


