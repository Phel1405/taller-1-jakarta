FROM quay.io/wildfly/wildfly:40.0.1.Final-jdk21
COPY target/jakartaee-logic-tier.war /opt/jboss/wildfly/standalone/deployments/
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-full.xml"]
