FROM java
VOLUME /tmp
ADD *.jar /app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar", "--server.port=80" ]