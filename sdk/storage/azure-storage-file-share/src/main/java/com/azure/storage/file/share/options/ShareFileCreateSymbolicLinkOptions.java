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

    public ShareFileCreateSymbolicLinkOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public OffsetDateTime getFileCreationTime() {
        return fileCreationTime;
    }

    public ShareFileCreateSymbolicLinkOptions setFileCreationTime(OffsetDateTime fileCreationTime) {
        this.fileCreationTime = fileCreationTime;
        return this;
    }

    public OffsetDateTime getFileLastWriteTime() {
        return fileLastWriteTime;
    }

    public ShareFileCreateSymbolicLinkOptions setFileLastWriteTime(OffsetDateTime fileLastWriteTime) {
        this.fileLastWriteTime = fileLastWriteTime;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public ShareFileCreateSymbolicLinkOptions setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ShareFileCreateSymbolicLinkOptions setGroup(String group) {
        this.group = group;
        return this;
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
