import React, { useState } from 'react';
import { getScreenshotImage } from '../services/api';

function ScreenshotGallery({ screenshots }) {
    const [lightboxImage, setLightboxImage] = useState(null);

    const formatDateTime = (dateTime) => {
        return new Date(dateTime).toLocaleString();
    };

    const formatFileSize = (bytes) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    };

    const openLightbox = (screenshotId) => {
        setLightboxImage(screenshotId);
    };

    const closeLightbox = () => {
        setLightboxImage(null);
    };

    return (
        <>
            <div className="screenshots-grid">
                {screenshots.map((screenshot) => (
                    <div
                        key={screenshot.id}
                        className="screenshot-card"
                        onClick={() => openLightbox(screenshot.id)}
                    >
                        <img
                            src={getScreenshotImage(screenshot.id)}
                            alt={`Screenshot ${screenshot.id}`}
                            className="screenshot-image"
                        />
                        <div className="screenshot-info">
                            <div className="screenshot-time">
                                📸 {formatDateTime(screenshot.capturedAt)}
                            </div>
                            <div className="screenshot-size">
                                Size: {formatFileSize(screenshot.fileSize)}
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {lightboxImage && (
                <div className="lightbox" onClick={closeLightbox}>
                    <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
                        <button className="lightbox-close" onClick={closeLightbox}>
                            ×
                        </button>
                        <img
                            src={getScreenshotImage(lightboxImage)}
                            alt="Screenshot"
                            className="lightbox-image"
                        />
                    </div>
                </div>
            )}
        </>
    );
}

export default ScreenshotGallery;
