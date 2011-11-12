package com.microsoft.windowsazure.services.blob.models;

public class CreateBlobBlockOptions extends BlobServiceOptions {
    private String leaseId;
    // TODO: Should the service layer support computing MD5 for the caller?
    private String contentMD5;

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
