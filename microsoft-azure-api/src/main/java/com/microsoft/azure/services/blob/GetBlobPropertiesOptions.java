package com.microsoft.azure.services.blob;

import java.util.Date;

public class GetBlobPropertiesOptions {
    private Date snapshot;
    private String leaseId;

    public Date getSnapshot() {
        return snapshot;
    }

    public GetBlobPropertiesOptions setSnapshot(Date snapshot) {
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
