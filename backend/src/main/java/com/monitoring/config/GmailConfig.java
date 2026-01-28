package com.monitoring.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GmailConfig {

    private static final String APPLICATION_NAME = "Employee Monitoring System";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);

    @Value("${gmail.credentials.file-path:src/main/resources/credentials.json}")
    private String credentialsFilePath;

    @Value("${gmail.tokens.directory-path:tokens}")
    private String tokensDirectoryPath;

    @Value("${GMAIL_TOKEN_BASE64:}")
    private String gmailTokenBase64;

    @Value("${GMAIL_CREDENTIALS_BASE64:}")
    private String gmailCredentialsBase64;

    @Bean
    public Gmail gmailService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Load client secrets.
        InputStream in;
        if (gmailCredentialsBase64 != null && !gmailCredentialsBase64.isEmpty()) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(gmailCredentialsBase64);
                in = new java.io.ByteArrayInputStream(decoded);
                System.out.println("Loaded credentials from GMAIL_CREDENTIALS_BASE64 environment variable.");
            } catch (IllegalArgumentException e) {
                throw new IOException("Failed to decode GMAIL_CREDENTIALS_BASE64", e);
            }
        } else {
            try {
                in = new FileInputStream(credentialsFilePath);
            } catch (FileNotFoundException e) {
                // Fallback to classpath resource if file path not found as absolute/relative
                // path
                in = GmailConfig.class.getResourceAsStream("/credentials.json");
                if (in == null) {
                    throw new FileNotFoundException("Resource not found: " + credentialsFilePath);
                }
            }
        }

        // Handle Base64 Token injection (for Render/Cloud deployment)
        if (gmailTokenBase64 != null && !gmailTokenBase64.isEmpty()) {
            java.io.File tokensDir = new java.io.File(tokensDirectoryPath);
            if (!tokensDir.exists()) {
                tokensDir.mkdirs();
            }
            java.io.File tokenFile = new java.io.File(tokensDir, "StoredCredential");
            if (!tokenFile.exists()) {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tokenFile)) {
                    byte[] tokenBytes = java.util.Base64.getDecoder().decode(gmailTokenBase64);
                    fos.write(tokenBytes);
                    System.out.println("Restored 'StoredCredential' from Base64 environment variable.");
                } catch (IllegalArgumentException e) {
                    System.err.println("Failed to decode GMAIL_TOKEN_BASE64: " + e.getMessage());
                }
            }
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        // This will open a browser window on the server if no tokens are found.
        // For production (deployed), you MUST clear the initial authentication
        // locally and copy the 'tokens' directory to the server.
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
