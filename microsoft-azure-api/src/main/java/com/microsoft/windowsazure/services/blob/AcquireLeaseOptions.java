package com.microsoft.windowsazure.services.blob;

public class AcquireLeaseOptions extends BlobOptions {
    private AccessCondition accessCondition;

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public AcquireLeaseOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
