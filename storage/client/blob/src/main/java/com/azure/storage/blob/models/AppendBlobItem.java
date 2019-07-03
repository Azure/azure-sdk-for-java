package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

public class AppendBlobItem {

    private OffsetDateTime lastModified;

    private final byte[] contentMD5;

    private Boolean isServerEncrypted;

    private String encryptionKeySha256;

    private String blobAppendOffset;

    private Integer blobCommittedBlockCount;

    public AppendBlobItem(AppendBlobCreateHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
    }

    public AppendBlobItem(AppendBlobAppendBlockHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
        this.blobAppendOffset = generatedHeaders.blobAppendOffset();
        this.blobCommittedBlockCount = generatedHeaders.blobCommittedBlockCount();
    }

    public AppendBlobItem(AppendBlobAppendBlockFromUrlHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.blobAppendOffset = generatedHeaders.blobAppendOffset();
        this.blobCommittedBlockCount = generatedHeaders.blobCommittedBlockCount();
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

    public String blobAppendOffset() {
        return blobAppendOffset;
    };

    public Integer blobCommittedBlockCount() {
        return blobCommittedBlockCount;
    };
}
