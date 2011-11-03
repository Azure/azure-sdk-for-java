package com.microsoft.azure.services.blob;

import java.io.InputStream;

public class Blob {
    private InputStream contentStream;
    private BlobProperties properties;

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public BlobProperties getProperties() {
        return properties;
    }

    public void setProperties(BlobProperties properties) {
        this.properties = properties;
    }
}
