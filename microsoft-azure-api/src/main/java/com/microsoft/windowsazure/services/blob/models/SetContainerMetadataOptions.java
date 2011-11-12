package com.microsoft.windowsazure.services.blob.models;


public class SetContainerMetadataOptions extends BlobOptions {
    private AccessCondition accessCondition;

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public SetContainerMetadataOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
