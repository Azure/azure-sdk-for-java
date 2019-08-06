package com.azure.storage.file.models;

/**
 * Contains metadata information about a Directory in the storage File service.
 */
public class DirectorySetMetadataInfo {
    private final String eTag;
    private final boolean isServerEncrypted;

    /**
     * Creates an instance of information about a specific Directory.
     *
     * @param eTag Entity tag that corresponds to the share
     * @param isServerEncrypted The value of this header is set to true if the directory metadata is completely encrypted using the specified algorithm. Otherwise, the value is set to false.
     */
    public DirectorySetMetadataInfo(final String eTag, final boolean isServerEncrypted) {
        this.eTag = eTag;
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * @return The entity tag that corresponds to the directory.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return The value of this header is true if the directory metadata is completely encrypted using the specified algorithm. Otherwise, the value is false.
     */
    public boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
