package com.microsoft.windowsazure.services.blob.models;

public class SetContainerMetadataOptions extends BlobServiceOptions {
    private AccessCondition accessCondition;

    @Override
    public SetContainerMetadataOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public SetContainerMetadataOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
