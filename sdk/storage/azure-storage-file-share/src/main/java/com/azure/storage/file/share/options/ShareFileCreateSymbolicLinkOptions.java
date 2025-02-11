package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ShareRequestConditions;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Options that may be passed when creating a symbolic link for a file.
 */
public class ShareFileCreateSymbolicLinkOptions {

    private Map<String, String> metadata;
    private  OffsetDateTime fileCreationTime;
    private OffsetDateTime fileLastWriteTime;
    private String owner;
    private String group;
    private ShareRequestConditions requestConditions;
    private final String linkText;

    public ShareFileCreateSymbolicLinkOptions(String linkText) {
        this.linkText = linkText;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public OffsetDateTime getFileCreationTime() {
        return fileCreationTime;
    }

    public void setFileCreationTime(OffsetDateTime fileCreationTime) {
        this.fileCreationTime = fileCreationTime;
    }

    public OffsetDateTime getFileLastWriteTime() {
        return fileLastWriteTime;
    }

    public void setFileLastWriteTime(OffsetDateTime fileLastWriteTime) {
        this.fileLastWriteTime = fileLastWriteTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }


    public ShareFileCreateSymbolicLinkOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    public String getLinkText() {
        return linkText;
    }

}
