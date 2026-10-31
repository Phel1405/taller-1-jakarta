FROM quay.io/wildfly/wildfly:40.0.1.Final-jdk21

ARG POSTGRES_DRIVER_VERSION=42.7.4
ARG MYSQL_DRIVER_VERSION=9.1.0

USER root

RUN mkdir -p /tmp/drivers \
    && curl -fsSL -o /tmp/drivers/postgresql.jar \
        "https://repo1.maven.org/maven2/org/postgresql/postgresql/${POSTGRES_DRIVER_VERSION}/postgresql-${POSTGRES_DRIVER_VERSION}.jar" \
    && curl -fsSL -o /tmp/drivers/mysql-connector-j.jar \
        "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_DRIVER_VERSION}/mysql-connector-j-${MYSQL_DRIVER_VERSION}.jar" \
    && chown -R jboss:jboss /tmp/drivers

COPY docker/configure-wildfly.cli /tmp/configure-wildfly.cli

USER jboss

# Registra los drivers como módulos y crea los xa-data-source PedidosDS/PagosDS
# directamente en standalone-full.xml (embed-server no necesita un servidor en
# ejecución: lee y escribe el XML de configuración sin arrancar WildFly).
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure-wildfly.cli

COPY target/jakartaee-data-tier.war /opt/jboss/wildfly/standalone/deployments/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-full.xml"]
