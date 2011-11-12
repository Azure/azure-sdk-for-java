package com.microsoft.windowsazure.services.blob;

import java.util.Date;

/*
 * TODO: Rename to "CreateBlobSnapshotResult"?
 */
public class BlobSnapshot {
    private String snapshot;
    private String etag;
    private Date lastModified;

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

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
