package com.microsoft.azure.services.blob;

public class SetBlobMetadataOptions extends BlobOptions {
    private String leaseId;
    private AccessCondition accessCondition;

    public String getLeaseId() {
        return leaseId;
    }

    public SetBlobMetadataOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public SetBlobMetadataOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
