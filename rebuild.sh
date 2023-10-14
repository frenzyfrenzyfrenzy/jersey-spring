#!/bin/zsh

docker ps -aq | xargs docker stop | xargs docker rm
mvn clean install
docker run -e JPDA_SUSPEND=y -d -p 8080:8080 -p 8000:8000 svintsov/nio-test