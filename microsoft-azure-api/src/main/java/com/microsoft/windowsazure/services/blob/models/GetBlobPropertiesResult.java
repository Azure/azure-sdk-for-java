package com.microsoft.windowsazure.services.blob.models;

import java.util.HashMap;

public class GetBlobPropertiesResult {
    private BlobProperties properties;
    private HashMap<String, String> metadata = new HashMap<String, String>();

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
