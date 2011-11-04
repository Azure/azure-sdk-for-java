package com.microsoft.azure.services.blob;

import java.util.HashMap;

public class CreateBlobSnapshotOptions {
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private String leaseId;
    //TODO: Add Ifxxx headers

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
}
