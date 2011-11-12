package com.microsoft.windowsazure.services.blob.models;

public class AcquireLeaseResult {
    private String leaseId;

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }
}
