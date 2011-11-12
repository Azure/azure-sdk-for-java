package com.microsoft.windowsazure.services.blob;

import java.util.Date;
import java.util.HashMap;

public class GetBlobMetadataResult {
    private String etag;
    private Date lastModified;
    private HashMap<String, String> metadata;

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

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
