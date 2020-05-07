// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import com.azure.storage.blob.models.BlobType;
import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.Map;

public class BlobChangefeedEventData {

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
    private final Boolean recursive;
    private final String sequencer;

    public BlobChangefeedEventData(String api, String clientRequestId, String requestId, String eTag,
        String contentType, Long contentLength, BlobType blobType, Long contentOffset, String destinationUrl,
        String sourceUrl, String blobUrl, Boolean recursive, String sequencer) {
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

    public static BlobChangefeedEventData fromRecord(Object d) {
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

        return new BlobChangefeedEventData(
            BlobChangefeedEvent.nullOrString("api", api),
            BlobChangefeedEvent.nullOrString("clientRequestId", clientRequestId),
            BlobChangefeedEvent.nullOrString("requestId", requestId),
            BlobChangefeedEvent.nullOrString("etag", eTag),
            BlobChangefeedEvent.nullOrString("contentType", contentType),
            BlobChangefeedEvent.nullOrLong("contentLength", contentLength),
            BlobChangefeedEvent.isNull(blobType) ? null
                : BlobType.fromString(BlobChangefeedEvent.nullOrString("blobType", blobType)),
            BlobChangefeedEvent.nullOrLong("contentOffset", contentOffset),
            BlobChangefeedEvent.nullOrString("destinationUrl", destinationUrl),
            BlobChangefeedEvent.nullOrString("sourceUrl", sourceUrl),
            BlobChangefeedEvent.nullOrString("url", blobUrl),
            BlobChangefeedEvent.nullOrBoolean("recursive", recursive),
            BlobChangefeedEvent.nullOrString("sequencer", sequencer)
        );
    }

    public String getApi() {
        return api;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String geteTag() {
        return eTag;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public BlobType getBlobType() {
        return blobType;
    }

    public Long getContentOffset() {
        return contentOffset;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getBlobUrl() {
        return blobUrl;
    }

    public Boolean getRecursive() {
        return recursive;
    }

    public String getSequencer() {
        return sequencer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlobChangefeedEventData)) return false;
        BlobChangefeedEventData that = (BlobChangefeedEventData) o;
        return getApi().equals(that.getApi())
            && getClientRequestId().equals(that.getClientRequestId())
            && getRequestId().equals(that.getRequestId())
            && geteTag().equals(that.geteTag())
            && getContentType().equals(that.getContentType())
            && getContentLength().equals(that.getContentLength())
            && getBlobType() == that.getBlobType()
            && getContentOffset().equals(that.getContentOffset()) &&
            getDestinationUrl().equals(that.getDestinationUrl()) &&
            getSourceUrl().equals(that.getSourceUrl()) &&
            getBlobUrl().equals(that.getBlobUrl()) &&
            getRecursive().equals(that.getRecursive())
            && getSequencer().equals(that.getSequencer());
    }
}
