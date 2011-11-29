package com.microsoft.windowsazure.services.blob.models;


public class AcquireLeaseOptions extends BlobServiceOptions {
    private AccessCondition accessCondition;

    @Override
    public AcquireLeaseOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public AcquireLeaseOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
