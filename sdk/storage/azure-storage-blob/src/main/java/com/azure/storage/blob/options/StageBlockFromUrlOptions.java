package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

@Fluent
public class StageBlockFromUrlOptions {
    private String base64BlockId;
    private String sourceUrl;
    private BlobRange sourceRange;
    private byte[] sourceContentMd5;
    private String leaseId;
    private BlobRequestConditions sourceRequestConditions;
    private String sourceBearerToken;

    public String getBase64BlockId() {
        return base64BlockId;
    }

    public StageBlockFromUrlOptions setBase64BlockId(String base64BlockId) {
        this.base64BlockId = base64BlockId;
        return this;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public StageBlockFromUrlOptions setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public BlobRange getSourceRange() {
        return sourceRange;
    }

    public StageBlockFromUrlOptions setSourceRange(BlobRange sourceRange) {
        this.sourceRange = sourceRange;
        return this;
    }

    public byte[] getSourceContentMd5() {
        return sourceContentMd5;
    }

    public StageBlockFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5) {
        this.sourceContentMd5 = sourceContentMd5;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public StageBlockFromUrlOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    public StageBlockFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    public String getSourceBearerToken() {
        return sourceBearerToken;
    }

    public StageBlockFromUrlOptions setSourceBearerToken(String sourceBearerToken) {
        this.sourceBearerToken = sourceBearerToken;
        return this;
    }
}
