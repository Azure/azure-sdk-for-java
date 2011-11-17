package com.microsoft.windowsazure.services.blob.models;

import java.io.InputStream;


public class GetBlobResult {
    private InputStream contentStream;
    private GetBlobPropertiesResult properties;

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public GetBlobPropertiesResult getProperties() {
        return properties;
    }

    public void setProperties(GetBlobPropertiesResult properties) {
        this.properties = properties;
    }
}
