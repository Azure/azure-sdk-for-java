package com.azure.storage.file.models;

public class FileMetadataInfo {
    private String eTag;
    private Boolean isServerEncrypted;

    public FileMetadataInfo(final String eTag, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.isServerEncrypted = isServerEncrypted;
    }

    public String eTag() {
        return eTag;
    }

    public FileMetadataInfo eTag(final String eTag) {
        this.eTag = eTag;
        return this;
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    public FileMetadataInfo isServerEncrypted(final Boolean serverEncrypted) {
        isServerEncrypted = serverEncrypted;
        return this;
    }
}
