package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.file.share.FileSmbProperties;

import java.time.OffsetDateTime;

@Immutable
public class ShareFileSymbolicLinkInfo {

    private final String eTag;
    private final OffsetDateTime lastModified;
    private final String linkText;

    public ShareFileSymbolicLinkInfo(String eTag, OffsetDateTime lastModified, String linkText) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.linkText = linkText;
    }

    public String getETag() {
        return eTag;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public String getLinkText() {
        return linkText;
    }
}
