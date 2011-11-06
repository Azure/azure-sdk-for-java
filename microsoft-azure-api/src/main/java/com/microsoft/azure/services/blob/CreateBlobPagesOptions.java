package com.microsoft.azure.services.blob;

public class CreateBlobPagesOptions {
    private String leaseId;
    private String contentMD5;
    private AccessCondition accessCondition;

    public String getLeaseId() {
        return leaseId;
    }

    public CreateBlobPagesOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public CreateBlobPagesOptions setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public CreateBlobPagesOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
