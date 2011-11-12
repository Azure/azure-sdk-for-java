package com.microsoft.windowsazure.services.blob.models;


public class ListBlobBlocksOptions extends BlobOptions {
    private String leaseId;
    private String snapshot;
    private String listType; // "committed", "uncommitted", "all"

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

    public String getListType() {
        return listType;
    }

    public ListBlobBlocksOptions setListType(String listType) {
        this.listType = listType;
        return this;
    }
}
