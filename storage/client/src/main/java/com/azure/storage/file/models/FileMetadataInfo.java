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

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
