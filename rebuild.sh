#!/bin/zsh

docker ps -aq | xargs docker stop | xargs docker rm
mvn clean install
docker run -e JPDA_SUSPEND=n \
-e CATALINA_OPTS="-Djava.rmi.server.hostname=$HOST -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.rmi.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false" \
-d -p 8080:8080 -p 8000:8000 -p 9010:9010 svintsov/nio-test