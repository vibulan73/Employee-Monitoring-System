# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/employee-monitoring-backend-1.0.0.jar app.jar

# Create entrypoint script to fix DB_URL
# This script prepends 'jdbc:' to the DB_URL if it's missing, which is common on Render
RUN echo '#!/bin/sh' > entrypoint.sh && \
    echo 'if [ -n "$DB_URL" ]; then' >> entrypoint.sh && \
    echo '  # Check if DB_URL starts with jdbc:' >> entrypoint.sh && \
    echo '  if [ "${DB_URL#jdbc:}" = "$DB_URL" ]; then' >> entrypoint.sh && \
    echo '    export DB_URL="jdbc:$DB_URL"' >> entrypoint.sh && \
    echo '  fi' >> entrypoint.sh && \
    echo 'fi' >> entrypoint.sh && \
    echo 'exec java -jar app.jar' >> entrypoint.sh && \
    chmod +x entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
