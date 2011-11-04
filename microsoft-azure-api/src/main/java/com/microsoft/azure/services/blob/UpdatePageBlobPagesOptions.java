package com.microsoft.azure.services.blob;

public class UpdatePageBlobPagesOptions {
    private String leaseId;
    private String contentMD5;

    // TODO: Add "if" from
    // http://msdn.microsoft.com/en-us/library/windowsazure/ee691975.aspx

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
