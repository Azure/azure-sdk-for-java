// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.Map;
import java.util.Objects;

/**
 * This class contains properties of a BlobChangefeedEventData.
 */
public class InternalBlobChangefeedEventData implements BlobChangefeedEventData {

    private final String api;
    private final String clientRequestId;
    private final String requestId;
    private final String eTag;
    private final String contentType;
    private final Long contentLength;
    private final BlobType blobType;
    private final Long contentOffset;
    private final String destinationUrl;
    private final String sourceUrl;
    private final String blobUrl;
    private final boolean recursive;
    private final String sequencer;

    /**
     * Constructs a {@link InternalBlobChangefeedEventData}.
     *
     * @param api The api.
     * @param clientRequestId The client request id.
     * @param requestId The request id.
     * @param eTag The eTag.
     * @param contentType The content type.
     * @param contentLength Th4e content length.
     * @param blobType {@link BlobType}
     * @param contentOffset The content offset.
     * @param destinationUrl The destination url.
     * @param sourceUrl The source url.
     * @param blobUrl The blob url.
     * @param recursive Whether or not this operation was recursive.
     * @param sequencer The sequencer.
     */
    public InternalBlobChangefeedEventData(String api, String clientRequestId, String requestId, String eTag,
        String contentType, Long contentLength, BlobType blobType, Long contentOffset, String destinationUrl,
        String sourceUrl, String blobUrl, boolean recursive, String sequencer) {
        this.api = api;
        this.clientRequestId = clientRequestId;
        this.requestId = requestId;
        this.eTag = eTag;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.blobType = blobType;
        this.contentOffset = contentOffset;
        this.destinationUrl = destinationUrl;
        this.sourceUrl = sourceUrl;
        this.blobUrl = blobUrl;
        this.recursive = recursive;
        this.sequencer = sequencer;
    }

    static InternalBlobChangefeedEventData fromRecord(Object d) {
        AvroSchema.checkType("data", d, Map.class);
        Map<?, ?> data = (Map<?, ?>) d;

        if (!data.get(AvroConstants.RECORD).equals("BlobChangeEventData")) {
            throw new IllegalArgumentException("Not a valid BlobChangefeedEventData.");
        }

        Object api = data.get("api");
        Object clientRequestId = data.get("clientRequestId");
        Object requestId = data.get("requestId");
        Object eTag = data.get("etag");
        Object contentType = data.get("contentType");
        Object contentLength = data.get("contentLength");
        Object blobType = data.get("blobType");
        Object contentOffset = data.get("contentOffset");
        Object destinationUrl = data.get("destinationUrl");
        Object sourceUrl = data.get("sourceUrl");
        Object blobUrl = data.get("url");
        Object recursive = data.get("recursive");
        Object sequencer = data.get("sequencer");

        return new InternalBlobChangefeedEventData(
            ChangefeedTypeValidator.nullOr("api", api, String.class),
            ChangefeedTypeValidator.nullOr("clientRequestId", clientRequestId, String.class),
            ChangefeedTypeValidator.nullOr("requestId", requestId, String.class),
            ChangefeedTypeValidator.nullOr("etag", eTag, String.class),
            ChangefeedTypeValidator.nullOr("contentType", contentType, String.class),
            ChangefeedTypeValidator.nullOr("contentLength", contentLength, Long.class),
            ChangefeedTypeValidator.isNull(blobType) ? null
                : BlobType.fromString(ChangefeedTypeValidator.nullOr("blobType", blobType, String.class)),
            ChangefeedTypeValidator.nullOr("contentOffset", contentOffset, Long.class),
            ChangefeedTypeValidator.nullOr("destinationUrl", destinationUrl, String.class),
            ChangefeedTypeValidator.nullOr("sourceUrl", sourceUrl, String.class),
            ChangefeedTypeValidator.nullOr("url", blobUrl, String.class),
            Boolean.TRUE.equals(ChangefeedTypeValidator.nullOr("recursive", recursive, Boolean.class)),
            ChangefeedTypeValidator.nullOr("sequencer", sequencer, String.class)
        );
    }

    @Override
    public String getApi() {
        return api;
    }

    @Override
    public String getClientRequestId() {
        return clientRequestId;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public String getETag() {
        return eTag;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Long getContentLength() {
        return contentLength;
    }

    @Override
    public BlobType getBlobType() {
        return blobType;
    }

    @Override
    public Long getContentOffset() {
        return contentOffset;
    }

    @Override
    public String getDestinationUrl() {
        return destinationUrl;
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    @Override
    public String getBlobUrl() {
        return blobUrl;
    }

    @Override
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public String getSequencer() {
        return sequencer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InternalBlobChangefeedEventData)) {
            return false;
        }
        InternalBlobChangefeedEventData that = (InternalBlobChangefeedEventData) o;
        return Objects.equals(getApi(), that.getApi())
            && Objects.equals(getClientRequestId(), that.getClientRequestId())
            && Objects.equals(getRequestId(), that.getRequestId())
            && Objects.equals(getETag(), that.getETag())
            && Objects.equals(getContentType(), that.getContentType())
            && Objects.equals(getContentLength(), that.getContentLength())
            && getBlobType() == that.getBlobType()
            && Objects.equals(getContentOffset(), that.getContentOffset())
            && Objects.equals(getDestinationUrl(), that.getDestinationUrl())
            && Objects.equals(getSourceUrl(), that.getSourceUrl())
            && Objects.equals(getBlobUrl(), that.getBlobUrl())
            && Objects.equals(isRecursive(), that.isRecursive())
            && Objects.equals(getSequencer(), that.getSequencer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApi(), getClientRequestId(), getRequestId(), getETag(), getContentType(),
            getContentLength(), getBlobType(), getContentOffset(), getDestinationUrl(), getSourceUrl(),
            getBlobUrl(), isRecursive(), getSequencer());
    }

    @Override
    public String toString() {
        return "BlobChangefeedEventData{"
            + "api='" + api + '\''
            + ", clientRequestId='" + clientRequestId + '\''
            + ", requestId='" + requestId + '\''
            + ", eTag='" + eTag + '\''
            + ", contentType='" + contentType + '\''
            + ", contentLength=" + contentLength
            + ", blobType=" + blobType
            + ", contentOffset=" + contentOffset
            + ", destinationUrl='" + destinationUrl + '\''
            + ", sourceUrl='" + sourceUrl + '\''
            + ", blobUrl='" + blobUrl + '\''
            + ", recursive=" + recursive
            + ", sequencer='" + sequencer + '\''
            + '}';
    }
}
