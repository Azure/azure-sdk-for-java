package com.microsoft.azure.services.blob;

public class DeleteBlobOptions {
    private String snapshot;
    private String leaseId;
    // TODO: Enum?
    private String deleteSnaphots;
    private AccessCondition accessCondition;

    public String getSnapshot() {
        return snapshot;
    }

    public DeleteBlobOptions setSnapshot(String snapshot) {
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

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public DeleteBlobOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
