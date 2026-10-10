FROM quay.io/wildfly/wildfly:40.0.1.Final-jdk21

ENV DATA_TIER_URL=http://data-tier:8080/jakartaee-data-tier/api

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --commands="embed-server --server-config=standalone-full.xml --std-out=echo, \
/subsystem=transactions:write-attribute(name=node-identifier,value=logic-tier), \
stop-embedded-server"

COPY target/jakartaee-logic-tier.war /opt/jboss/wildfly/standalone/deployments/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-full.xml"]
