FROM payara/server-web:6.2025.1-jdk17
COPY target/jakartaee-spaced-repetition.war $DEPLOY_DIR
