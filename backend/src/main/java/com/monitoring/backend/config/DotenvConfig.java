package com.monitoring.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class DotenvConfig {

    static {
        // Load .env file
        // We look for .env in the project root (current working directory)
        File envFile = new File(".env");
        if (envFile.exists()) {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // Populate System properties with .env vars so that Spring can see them
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            System.out.println("Loaded environment variables from .env file");
        } else {
            System.out.println("No .env file found, skipping environment variable loading");
        }
    }
}
