package com.monitoring.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Gmail gmailService;

    @Value("${monitoring.admin.email}")
    private String adminEmails;

    @Value("${monitoring.admin.cc-email:}")
    private String ccEmails;

    @Value("${monitoring.admin.bcc-email:}")
    private String bccEmails;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public void sendIdleWarningEmail(UUID sessionId, String userId, String firstName, String lastName,
            String jobRole, int idleMinutes) {
        try {
            String subject = "⚠️ Idle Alert: " + firstName + " " + lastName + " - " + idleMinutes + " Minutes Idle";
            String body = buildIdleWarningEmailBody(sessionId, userId, firstName, lastName, jobRole, idleMinutes);

            String[] to = parseEmailList(adminEmails);
            String[] cc = parseEmailList(ccEmails);
            String[] bcc = parseEmailList(bccEmails);

            sendHtmlEmail(to, cc, bcc, subject, body);
            log.info("Idle warning email sent for user {} ({})", userId, sessionId);
        } catch (Exception e) {
            log.error("Failed to send idle warning email for user {}", userId, e);
        }
    }

    public void sendAutoStopEmail(UUID sessionId, String userId, String firstName, String lastName,
            String jobRole, int idleMinutes) {
        try {
            String subject = "🛑 Session Auto-Stopped: " + firstName + " " + lastName + " - " + idleMinutes
                    + "+ Minutes Idle";
            String body = buildAutoStopEmailBody(sessionId, userId, firstName, lastName, jobRole, idleMinutes);

            String[] to = parseEmailList(adminEmails);
            String[] cc = parseEmailList(ccEmails);
            String[] bcc = parseEmailList(bccEmails);

            sendHtmlEmail(to, cc, bcc, subject, body);
            log.info("Auto-stop notification email sent for user {} ({})", userId, sessionId);
        } catch (Exception e) {
            log.error("Failed to send auto-stop email for user {}", userId, e);
        }
    }

    private void sendHtmlEmail(String[] to, String[] cc, String[] bcc, String subject, String htmlBody)
            throws MessagingException, IOException {

        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        if (cc != null && cc.length > 0) {
            helper.setCc(cc);
        }
        if (bcc != null && bcc.length > 0) {
            helper.setBcc(bcc);
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.setFrom("vibul.try@gmail.com"); // Still used for MimeMessage, but Gmail API uses authenticated user's
                                               // address

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        gmailService.users().messages().send("me", message).execute();
    }

    private String[] parseEmailList(String emailString) {
        if (emailString == null || emailString.trim().isEmpty()) {
            return new String[0];
        }
        return emailString.split("\\s*,\\s*");
    }

    private String buildIdleWarningEmailBody(UUID sessionId, String userId, String firstName,
            String lastName, String jobRole, int idleMinutes) {
        String currentTime = LocalDateTime.now().format(formatter);

        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: #f39c12; color: white; padding: 20px; border-radius: 5px 5px 0 0; }
                                .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                                .user-details { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #f39c12; }
                                .detail-row { margin: 8px 0; }
                                .label { font-weight: bold; color: #666; }
                                .footer { background: #333; color: white; padding: 15px; border-radius: 0 0 5px 5px; text-align: center; font-size: 12px; }
                                .warning-box { background: #fff3cd; border: 1px solid #f39c12; padding: 15px; margin: 15px 0; border-radius: 5px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h2>⚠️ Idle Activity Alert</h2>
                                </div>
                                <div class="content">
                                    <p>Dear Admin,</p>
                                    <p>An employee has been idle for an extended period and requires your attention.</p>

                                    <div class="user-details">
                                        <h3 style="margin-top: 0;">Employee Details</h3>
                                        <div class="detail-row"><span class="label">Name:</span> %s %s</div>
                                        <div class="detail-row"><span class="label">Job Role:</span> %s</div>
                                        <div class="detail-row"><span class="label">Employee ID:</span> %s</div>
                                        <div class="detail-row"><span class="label">Session ID:</span> %s</div>
                                    </div>

                                    <div class="warning-box">
                                        <strong>⏱️ Idle Duration:</strong> %d consecutive minutes<br>
                                        <strong>📅 Time:</strong> %s
                                    </div>

                                    <p><strong>⚠️ Important:</strong> The session will be automatically stopped if the employee continues to be idle for a total of 60 minutes.</p>
                                </div>
                                <div class="footer">
                                    <p>Employee Activity Monitoring System</p>
                                    <p>This is an automated notification. Please do not reply to this email.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                firstName, lastName, jobRole, userId, sessionId, idleMinutes, currentTime);
    }

    private String buildAutoStopEmailBody(UUID sessionId, String userId, String firstName,
            String lastName, String jobRole, int idleMinutes) {
        String currentTime = LocalDateTime.now().format(formatter);

        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: #e74c3c; color: white; padding: 20px; border-radius: 5px 5px 0 0; }
                                .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                                .user-details { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #e74c3c; }
                                .detail-row { margin: 8px 0; }
                                .label { font-weight: bold; color: #666; }
                                .footer { background: #333; color: white; padding: 15px; border-radius: 0 0 5px 5px; text-align: center; font-size: 12px; }
                                .alert-box { background: #f8d7da; border: 1px solid #e74c3c; padding: 15px; margin: 15px 0; border-radius: 5px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h2>🛑 Session Automatically Stopped</h2>
                                </div>
                                <div class="content">
                                    <p>Dear Admin,</p>
                                    <p>A work session has been automatically stopped due to prolonged inactivity.</p>

                                    <div class="user-details">
                                        <h3 style="margin-top: 0;">Employee Details</h3>
                                        <div class="detail-row"><span class="label">Name:</span> %s %s</div>
                                        <div class="detail-row"><span class="label">Job Role:</span> %s</div>
                                        <div class="detail-row"><span class="label">Employee ID:</span> %s</div>
                                        <div class="detail-row"><span class="label">Session ID:</span> %s</div>
                                    </div>

                                    <div class="alert-box">
                                        <strong>⏱️ Total Idle Time:</strong> %d+ consecutive minutes<br>
                                        <strong>🛑 Action Taken:</strong> Session automatically stopped<br>
                                        <strong>📅 Time:</strong> %s
                                    </div>

                                    <p><strong>Note:</strong> The employee may need to be contacted to verify their work status.</p>
                                </div>
                                <div class="footer">
                                    <p>Employee Activity Monitoring System</p>
                                    <p>This is an automated notification. Please do not reply to this email.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                firstName, lastName, jobRole, userId, sessionId, idleMinutes, currentTime);
    }
}
