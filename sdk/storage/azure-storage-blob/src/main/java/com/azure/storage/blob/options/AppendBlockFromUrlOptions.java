package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

@Fluent
public class AppendBlockFromUrlOptions {
    private String sourceUrl;
    private BlobRange sourceRange;
    private byte[] sourceContentMD5;
    private AppendBlobRequestConditions destinationRequestConditions;
    private BlobRequestConditions sourceRequestConditions;
    private String sourceBearerToken;

    public String getSourceUrl() {
        return sourceUrl;
    }

    public AppendBlockFromUrlOptions setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public BlobRange getSourceRange() {
        return sourceRange;
    }

    public AppendBlockFromUrlOptions setSourceRange(BlobRange sourceRange) {
        this.sourceRange = sourceRange;
        return this;
    }

    public byte[] getSourceContentMD5() {
        return sourceContentMD5;
    }

    public AppendBlockFromUrlOptions setSourceContentMD5(byte[] sourceContentMD5) {
        this.sourceContentMD5 = sourceContentMD5;
        return this;
    }

    public AppendBlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    public AppendBlockFromUrlOptions setDestinationRequestConditions(AppendBlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    public AppendBlockFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    public String getSourceBearerToken() {
        return sourceBearerToken;
    }

    public AppendBlockFromUrlOptions setSourceBearerToken(String sourceBearerToken) {
        this.sourceBearerToken = sourceBearerToken;
        return this;
    }
}
