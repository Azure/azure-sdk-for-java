package com.microsoft.windowsazure.services.blob.models;

import java.util.HashMap;

public class CreateContainerOptions extends BlobServiceOptions {
    private String publicAccess;
    private HashMap<String, String> metadata = new HashMap<String, String>();

    @Override
    public CreateContainerOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public CreateContainerOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public CreateContainerOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    public String getPublicAccess() {
        return publicAccess;
    }

    public CreateContainerOptions setPublicAccess(String publicAccess) {
        this.publicAccess = publicAccess;
        return this;
    }
}
