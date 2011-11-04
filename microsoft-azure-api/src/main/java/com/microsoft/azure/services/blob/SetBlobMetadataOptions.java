package com.microsoft.azure.services.blob;

public class SetBlobMetadataOptions {
    private String leaseId;

    public String getLeaseId() {
        return leaseId;
    }

    public SetBlobMetadataOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }
}
