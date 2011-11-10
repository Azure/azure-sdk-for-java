package com.microsoft.azure.services.blob;

import java.util.Date;

public class CreateBlobPagesResult {
    //TODO: For "Result" classes, should we include "Date", "x-ms-version" response headers?
    private String etag;
    private Date lastModified;
    private String contentMD5;
    private long sequenceNumber;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
