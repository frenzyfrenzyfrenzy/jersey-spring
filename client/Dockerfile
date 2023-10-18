FROM arm64v8/tomcat:10.1.0

COPY target/nio-test-client-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/nio-test-client.war

ENV JPDA_SUSPEND=n
ENV JPDA_ADDRESS=*:8000
ENV JPDA_TRANSPORT=dt_socket

EXPOSE 8080
EXPOSE 8000

CMD bin/catalina.sh jpda run