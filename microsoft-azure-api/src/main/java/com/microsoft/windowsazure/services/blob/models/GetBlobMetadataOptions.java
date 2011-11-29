package com.microsoft.windowsazure.services.blob.models;

public class GetBlobMetadataOptions extends BlobServiceOptions {
    private String snapshot;
    private String leaseId;
    private AccessCondition accessCondition;

    @Override
    public GetBlobMetadataOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

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
