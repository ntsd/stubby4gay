FROM	dockerfile/java:oracle-java8
MAINTAINER varkockova.a@gmail.com

RUN apt-get update

RUN wget https://repo1.maven.org/maven2/io/github/ntsd/stubby4gay/5.0.0/stubby4gay-5.0.0.jar

EXPOSE 8882 8889

ENTRYPOINT ["java", "-jar", "stubby4gay-5.0.0.jar"]
