package com.azure.storage.file.share.models;

import java.time.OffsetDateTime;

public class ShareFileItemProperties {
    private final OffsetDateTime createdOn;
    private final OffsetDateTime lastAccessedOn;
    private final OffsetDateTime lastWrittenOn;
    private final OffsetDateTime changedOn;
    private final OffsetDateTime lastModified;
    private final String eTag;

    public ShareFileItemProperties(OffsetDateTime createdOn, OffsetDateTime lastAccessedOn,
        OffsetDateTime lastWrittenOn, OffsetDateTime changedOn, OffsetDateTime lastModified, String eTag) {
        this.createdOn = createdOn;
        this.lastAccessedOn = lastAccessedOn;
        this.lastWrittenOn = lastWrittenOn;
        this.changedOn = changedOn;
        this.lastModified = lastModified;
        this.eTag = eTag;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public OffsetDateTime getLastAccessedOn() {
        return lastAccessedOn;
    }

    public OffsetDateTime getLastWrittenOn() {
        return lastWrittenOn;
    }

    public OffsetDateTime getChangedOn() {
        return changedOn;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public String geteTag() {
        return eTag;
    }
}
