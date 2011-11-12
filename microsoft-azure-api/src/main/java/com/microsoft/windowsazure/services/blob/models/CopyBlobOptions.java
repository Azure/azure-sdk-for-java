package com.microsoft.windowsazure.services.blob.models;

import java.util.HashMap;

public class CopyBlobOptions extends BlobOptions {
    private String leaseId;
    private AccessCondition accessCondition;
    private String sourceLeaseId;
    private String sourceSnapshot;
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private AccessCondition sourceAccessCondition;

    public String getSourceSnapshot() {
        return sourceSnapshot;
    }

    public CopyBlobOptions setSourceSnapshot(String sourceSnapshot) {
        this.sourceSnapshot = sourceSnapshot;
        return this;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public CopyBlobOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public CopyBlobOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public CopyBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public String getSourceLeaseId() {
        return sourceLeaseId;
    }

    public CopyBlobOptions setSourceLeaseId(String sourceLeaseId) {
        this.sourceLeaseId = sourceLeaseId;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public CopyBlobOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    public AccessCondition getSourceAccessCondition() {
        return sourceAccessCondition;
    }

    public CopyBlobOptions setSourceAccessCondition(AccessCondition sourceAccessCondition) {
        this.sourceAccessCondition = sourceAccessCondition;
        return this;
    }
}
