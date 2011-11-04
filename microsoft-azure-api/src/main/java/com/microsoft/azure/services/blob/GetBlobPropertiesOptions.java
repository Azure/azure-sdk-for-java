package com.microsoft.azure.services.blob;


public class GetBlobPropertiesOptions {
    private String snapshot;
    private String leaseId;

    public String getSnapshot() {
        return snapshot;
    }

    public GetBlobPropertiesOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public GetBlobPropertiesOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }
}
