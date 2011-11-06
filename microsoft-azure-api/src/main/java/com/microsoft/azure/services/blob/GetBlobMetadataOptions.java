package com.microsoft.azure.services.blob;


public class GetBlobMetadataOptions {
    private String snapshot;
    private String leaseId;
    private AccessCondition accessCondition;

    public String getSnapshot() {
        return snapshot;
    }

    public GetBlobMetadataOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public GetBlobMetadataOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public GetBlobMetadataOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
