package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

public class BlockBlobItem {

    private OffsetDateTime lastModified;

    private final byte[] contentMD5;

    private Boolean isServerEncrypted;

    private String encryptionKeySha256;

    public BlockBlobItem(BlockBlobUploadHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
    }

    public BlockBlobItem(BlockBlobCommitBlockListHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    };

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    public String encryptionKeySha256() {
        return encryptionKeySha256;
    }

    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }
}
