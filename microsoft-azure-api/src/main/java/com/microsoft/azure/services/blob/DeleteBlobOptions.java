package com.microsoft.azure.services.blob;

public class DeleteBlobOptions extends BlobOptions {
    private String snapshot;
    private String leaseId;
    private boolean deleteSnaphotsOnly;
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

    public boolean getDeleteSnaphotsOnly() {
        return deleteSnaphotsOnly;
    }

    public DeleteBlobOptions setDeleteSnaphotsOnly(boolean deleteSnaphotsOnly) {
        this.deleteSnaphotsOnly = deleteSnaphotsOnly;
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
