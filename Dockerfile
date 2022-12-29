FROM adoptopenjdk/openjdk14

COPY target/contentAudit-0.0.1-SNAPSHOT.jar look-see.jar
COPY GCP-MyFirstProject-1c31159db52c.json GCP-MyFirstProject-1c31159db52c.json
#COPY api_key.p12 /etc/certs/api_key.p12
COPY gmail_credentials.json /etc/creds/gmail_credentials.json
EXPOSE 443
EXPOSE 80
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Xms1G", "-ea","-jar", "look-see.jar"]
