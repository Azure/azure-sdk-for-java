package com.microsoft.azure.services.blob;

import java.util.Date;

public class GetBlobPropertiesOptions {
    private Date snapshot;
    private String leaseId;

    public Date getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Date snapshot) {
        this.snapshot = snapshot;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }
}
