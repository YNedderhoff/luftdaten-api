FROM anapsix/alpine-java

ADD target/luftdaten-api-*.jar /luftdaten-api/luftdaten-api.jar

WORKDIR /luftdaten-api/

EXPOSE 8080

CMD ["java", "-jar", "luftdaten-api.jar"]
