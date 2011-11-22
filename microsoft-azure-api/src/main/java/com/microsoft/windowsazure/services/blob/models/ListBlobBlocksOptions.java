package com.microsoft.windowsazure.services.blob.models;

public class ListBlobBlocksOptions extends BlobServiceOptions {
    private String leaseId;
    private String snapshot;
    private boolean committedList;
    private boolean uncommittedList;

    public String getLeaseId() {
        return leaseId;
    }

    public ListBlobBlocksOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public ListBlobBlocksOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public boolean isCommittedList() {
        return committedList;
    }

    public ListBlobBlocksOptions setCommittedList(boolean committedList) {
        this.committedList = committedList;
        return this;
    }

    public boolean isUncommittedList() {
        return uncommittedList;
    }

    public ListBlobBlocksOptions setUncommittedList(boolean uncommittedList) {
        this.uncommittedList = uncommittedList;
        return this;
    }
}
