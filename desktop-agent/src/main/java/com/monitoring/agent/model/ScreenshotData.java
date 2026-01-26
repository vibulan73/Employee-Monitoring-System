package com.monitoring.agent.model;

import java.io.File;

/**
 * Container for screenshot data including the file and metadata.
 */
public class ScreenshotData {
    private final File file;
    private final String metadata;

    public ScreenshotData(File file, String metadata) {
        this.file = file;
        this.metadata = metadata;
    }

    public File getFile() {
        return file;
    }

    public String getMetadata() {
        return metadata;
    }
}
