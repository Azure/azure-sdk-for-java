package com.microsoft.windowsazure.services.blob.models;

import java.util.HashMap;

public class CreateBlobSnapshotOptions extends BlobServiceOptions {
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private String leaseId;
    private AccessCondition accessCondition;

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public CreateBlobSnapshotOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public CreateBlobSnapshotOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public CreateBlobSnapshotOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public CreateBlobSnapshotOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
