package com.microsoft.azure.services.blob;

import java.util.HashMap;

public class CopyBlobOptions {
    private String leaseId;
    private String sourceLeaseId;
    private String sourceSnapshot;
    private HashMap<String, String> metadata = new HashMap<String, String>();

    // TODO: IfMatch options

    public String getSourceSnapshot() {
        return sourceSnapshot;
    }

    public void setSourceSnapshot(String sourceSnapshot) {
        this.sourceSnapshot = sourceSnapshot;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public String getSourceLeaseId() {
        return sourceLeaseId;
    }

    public void setSourceLeaseId(String sourceLeaseId) {
        this.sourceLeaseId = sourceLeaseId;
    }
}
