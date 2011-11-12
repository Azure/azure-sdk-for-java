package com.microsoft.windowsazure.services.blob;

import java.util.Date;

public class SetBlobMetadataResult {
    private String etag;
    private Date lastModified;

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
}
