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
# This script converts 'postgres://user:pass@host/db' to 'jdbc:postgresql://host/db'
RUN echo '#!/bin/sh' > entrypoint.sh && \
    echo 'if [ -n "$DB_URL" ]; then' >> entrypoint.sh && \
    echo '  # 1. Replace postgres:// with postgresql://' >> entrypoint.sh && \
    echo '  clean_url=$(echo "$DB_URL" | sed "s|^postgres://|postgresql://|")' >> entrypoint.sh && \
    echo '  # 2. Strip user:pass@ part (matches //...number...@)' >> entrypoint.sh && \
    echo '  clean_url=$(echo "$clean_url" | sed "s|//.*@|//|")' >> entrypoint.sh && \
    echo '  # 3. Prepend jdbc: if missing' >> entrypoint.sh && \
    echo '  if [ "${clean_url#jdbc:}" = "$clean_url" ]; then' >> entrypoint.sh && \
    echo '    clean_url="jdbc:$clean_url"' >> entrypoint.sh && \
    echo '  fi' >> entrypoint.sh && \
    echo '  export DB_URL="$clean_url"' >> entrypoint.sh && \
    echo 'fi' >> entrypoint.sh && \
    echo 'exec java -Djava.net.preferIPv4Stack=true -jar app.jar' >> entrypoint.sh && \
    chmod +x entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
