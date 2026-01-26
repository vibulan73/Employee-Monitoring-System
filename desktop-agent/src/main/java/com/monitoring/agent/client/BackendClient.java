package com.monitoring.agent.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monitoring.agent.model.AuthResponse;
import com.monitoring.agent.model.LoginRequest;
import com.monitoring.agent.model.SessionResponse;
import com.monitoring.agent.model.SignupRequest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BackendClient {
    private static final Logger logger = LoggerFactory.getLogger(BackendClient.class);

    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public AuthResponse signup(SignupRequest request) throws IOException, ParseException {
        String url = baseUrl + "/api/auth/signup";
        HttpPost httpPost = new HttpPost(url);

        String json = objectMapper.writeValueAsString(request);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getCode();

            if (statusCode == 201) {
                logger.info("Signup successful");
                return objectMapper.readValue(responseBody, AuthResponse.class);
            } else {
                logger.error("Signup failed: {}", responseBody);
                throw new IOException("Signup failed: " + responseBody);
            }
        }
    }

    public AuthResponse login(LoginRequest request) throws IOException, ParseException {
        String url = baseUrl + "/api/auth/login";
        HttpPost httpPost = new HttpPost(url);

        String json = objectMapper.writeValueAsString(request);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getCode();

            if (statusCode == 200) {
                logger.info("Login successful");
                return objectMapper.readValue(responseBody, AuthResponse.class);
            } else {
                logger.error("Login failed: {}", responseBody);
                throw new IOException("Login failed: " + responseBody);
            }
        }
    }

    public SessionResponse startSession(String userId) throws IOException, ParseException {
        String url = baseUrl + "/api/sessions/start";
        HttpPost request = new HttpPost(url);

        String json = String.format("{\"userId\":\"%s\"}", userId);
        request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getCode();

            if (statusCode == 201) {
                logger.info("Session started: {}", responseBody);
                return objectMapper.readValue(responseBody, SessionResponse.class);
            } else if (statusCode == 403) {
                // Tracking not allowed due to login rule restrictions
                logger.warn("Tracking not permitted: {}", responseBody);
                throw new IOException("TRACKING_NOT_ALLOWED: " + responseBody);
            } else {
                logger.error("Failed to start session. Status: {}, Response: {}", statusCode, responseBody);
                throw new IOException("Failed to start session: " + responseBody);
            }
        }
    }

    public SessionResponse stopSession(UUID sessionId) throws IOException, ParseException {
        String url = baseUrl + "/api/sessions/" + sessionId + "/stop";
        HttpPost request = new HttpPost(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.info("Session stopped: {}", responseBody);
            return objectMapper.readValue(responseBody, SessionResponse.class);
        }
    }

    public void logActivity(UUID sessionId, String activityStatus, String metadata) throws IOException {
        String url = baseUrl + "/api/activity";
        HttpPost request = new HttpPost(url);

        String json = String.format(
                "{\"sessionId\":\"%s\",\"activityStatus\":\"%s\",\"metadata\":\"%s\"}",
                sessionId, activityStatus, metadata != null ? metadata : "");
        request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
            logger.debug("Activity logged: {}", activityStatus);
        }
    }

    public void uploadScreenshot(UUID sessionId, File screenshotFile, String metadata) throws IOException {
        String url = baseUrl + "/api/screenshots";
        HttpPost request = new HttpPost(url);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("sessionId", sessionId.toString());
        builder.addBinaryBody("file", screenshotFile, ContentType.IMAGE_PNG, screenshotFile.getName());

        // Add metadata if available
        if (metadata != null && !metadata.isEmpty()) {
            builder.addTextBody("metadata", metadata, ContentType.TEXT_PLAIN);
        }

        request.setEntity(builder.build());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
            logger.info("Screenshot uploaded: {} with metadata: {}", screenshotFile.getName(), metadata);
        }
    }

    public SessionResponse getSession(UUID sessionId) throws IOException, ParseException {
        String url = baseUrl + "/api/sessions/" + sessionId;
        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return objectMapper.readValue(responseBody, SessionResponse.class);
        }
    }

    public void close() throws IOException {
        httpClient.close();
    }
}
