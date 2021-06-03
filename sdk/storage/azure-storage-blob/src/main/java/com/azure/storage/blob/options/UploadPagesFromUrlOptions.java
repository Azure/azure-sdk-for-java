package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;

@Fluent
public class UploadPagesFromUrlOptions {
    private PageRange range;
    private String sourceUrl;
    private Long sourceOffset;
    private byte[] sourceContentMd5;
    private PageBlobRequestConditions destinationRequestConditions;
    private BlobRequestConditions sourceRequestConditions;
    private String sourceBearerToken;

    public PageRange getRange() {
        return range;
    }

    public UploadPagesFromUrlOptions setRange(PageRange range) {
        this.range = range;
        return this;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public UploadPagesFromUrlOptions setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public Long getSourceOffset() {
        return sourceOffset;
    }

    public UploadPagesFromUrlOptions setSourceOffset(Long sourceOffset) {
        this.sourceOffset = sourceOffset;
        return this;
    }

    public byte[] getSourceContentMd5() {
        return sourceContentMd5;
    }

    public UploadPagesFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5) {
        this.sourceContentMd5 = sourceContentMd5;
        return this;
    }

    public PageBlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    public UploadPagesFromUrlOptions setDestinationRequestConditions(PageBlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    public UploadPagesFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    public String getSourceBearerToken() {
        return sourceBearerToken;
    }

    public UploadPagesFromUrlOptions setSourceBearerToken(String sourceBearerToken) {
        this.sourceBearerToken = sourceBearerToken;
        return this;
    }
}
