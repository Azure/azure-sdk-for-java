package com.microsoft.windowsazure.services.blob.models;

public class DeleteContainerOptions extends BlobServiceOptions {
    private AccessCondition accessCondition;

    @Override
    public DeleteContainerOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public DeleteContainerOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
