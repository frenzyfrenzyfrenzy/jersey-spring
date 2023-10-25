FROM arm64v8/tomcat:11.0

COPY target/nio-test-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/nio-test.war
#COPY logging.properties /usr/local/tomcat/conf/logging.properties

ENV JPDA_ADDRESS=*:8000
ENV JPDA_TRANSPORT=dt_socket

EXPOSE 8080
EXPOSE 8000
EXPOSE 9010

CMD bin/catalina.sh jpda run