package com.microsoft.windowsazure.services.blob;

public class GetBlobPropertiesOptions extends BlobOptions {
    private String snapshot;
    private String leaseId;
    private AccessCondition accessCondition;

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

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public GetBlobPropertiesOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
