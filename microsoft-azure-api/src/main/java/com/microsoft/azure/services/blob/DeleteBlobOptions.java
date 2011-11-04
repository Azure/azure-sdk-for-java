package com.microsoft.azure.services.blob;

import java.util.Date;

public class DeleteBlobOptions {
    //TODO: Date?
    private Date snapshot;
    private String leaseId;
    //TODO: Enum?
    private String deleteSnaphots;

    public Date getSnapshot() {
        return snapshot;
    }

    public DeleteBlobOptions setSnapshot(Date snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public DeleteBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public String getDeleteSnaphots() {
        return deleteSnaphots;
    }

    public DeleteBlobOptions setDeleteSnaphots(String deleteSnaphots) {
        this.deleteSnaphots = deleteSnaphots;
        return this;
    }
}
