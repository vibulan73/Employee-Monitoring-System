package com.monitoring.agent.monitor;

import com.monitoring.agent.model.ScreenshotData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenshotCapture {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotCapture.class);

    private final Robot robot;
    private final Rectangle screenRect;
    private final WindowMetadataCollector metadataCollector;

    public ScreenshotCapture() throws AWTException {
        this.robot = new Robot();

        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenRect = new Rectangle(screenSize);

        this.metadataCollector = new WindowMetadataCollector();
    }

    /**
     * Captures a screenshot along with active window metadata.
     * 
     * @param filePrefix Prefix for the temporary file name
     * @return ScreenshotData containing the file and metadata
     * @throws IOException if screenshot cannot be saved
     */
    public ScreenshotData captureScreenshot(String filePrefix) throws IOException {
        // Collect metadata BEFORE taking screenshot for better accuracy
        String metadata = metadataCollector.collectMetadata();

        // Capture screenshot
        BufferedImage screenshot = robot.createScreenCapture(screenRect);

        // Create temporary file
        File tempFile = File.createTempFile(filePrefix + "_", ".png");
        tempFile.deleteOnExit();

        // Save to file
        ImageIO.write(screenshot, "png", tempFile);

        logger.info("Screenshot captured: {} with metadata: {}", tempFile.getAbsolutePath(), metadata);
        return new ScreenshotData(tempFile, metadata);
    }
}
