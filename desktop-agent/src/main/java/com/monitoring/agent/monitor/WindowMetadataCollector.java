package com.monitoring.agent.monitor;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects metadata about the active window for screenshot context.
 * Uses JNA to access Windows native APIs.
 */
public class WindowMetadataCollector {
    private static final Logger logger = LoggerFactory.getLogger(WindowMetadataCollector.class);
    private static final int MAX_TITLE_LENGTH = 1024;

    /**
     * Captures metadata about the currently active window.
     * 
     * @return JSON-formatted string with window title and process info, or null if
     *         unable to collect
     */
    public String collectMetadata() {
        try {
            // Get the foreground window handle
            HWND hwnd = User32.INSTANCE.GetForegroundWindow();

            if (hwnd == null) {
                logger.warn("No foreground window found");
                return null;
            }

            // Get window title
            char[] windowText = new char[MAX_TITLE_LENGTH];
            User32.INSTANCE.GetWindowText(hwnd, windowText, MAX_TITLE_LENGTH);
            String windowTitle = Native.toString(windowText);

            // Get process ID
            com.sun.jna.ptr.IntByReference processId = new com.sun.jna.ptr.IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, processId);

            // Format metadata as simple JSON
            String metadata = String.format(
                    "{\"windowTitle\":\"%s\",\"processId\":%d}",
                    escapeJson(windowTitle),
                    processId.getValue());

            logger.debug("Collected window metadata: {}", metadata);
            return metadata;

        } catch (Exception e) {
            logger.error("Failed to collect window metadata", e);
            return null;
        }
    }

    /**
     * Escape special characters for JSON string values.
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
