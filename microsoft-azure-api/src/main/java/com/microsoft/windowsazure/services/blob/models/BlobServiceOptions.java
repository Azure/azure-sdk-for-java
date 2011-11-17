package com.microsoft.windowsazure.services.blob.models;

public class BlobServiceOptions {
    // Nullable because it is optional
    private Integer timeout;

    public Integer getTimeout() {
        return timeout;
    }

    public BlobServiceOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }
}
