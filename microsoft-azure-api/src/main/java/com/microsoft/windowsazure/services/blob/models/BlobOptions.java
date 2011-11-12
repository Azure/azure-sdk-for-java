package com.microsoft.windowsazure.services.blob.models;

public class BlobOptions {
    // Nullable because it is optional
    private Integer timeout;

    public Integer getTimeout() {
        return timeout;
    }

    public BlobOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }
}
