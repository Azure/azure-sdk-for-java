package com.microsoft.windowsazure.services.blob.models;

public class CreateBlobBlockOptions extends BlobServiceOptions {
    private String leaseId;
    private String contentMD5;

    @Override
    public CreateBlobBlockOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }
}
