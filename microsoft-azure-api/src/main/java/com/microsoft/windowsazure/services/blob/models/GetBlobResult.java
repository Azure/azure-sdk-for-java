package com.microsoft.windowsazure.services.blob.models;

import java.io.InputStream;
import java.util.HashMap;

public class GetBlobResult {
    private InputStream contentStream;
    private BlobProperties properties;
    private HashMap<String, String> metadata;

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

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
