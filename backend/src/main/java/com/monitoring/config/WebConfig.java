package com.monitoring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${monitoring.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${monitoring.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${monitoring.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${monitoring.cors.allow-credentials}")
    private boolean allowCredentials;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials);
    }
}
