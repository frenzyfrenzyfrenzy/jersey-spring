FROM arm64v8/tomcat:11.0

COPY target/nio-test-1.0-SNAPSHOT.war /usr/local/tomcat/webapps

EXPOSE 8080