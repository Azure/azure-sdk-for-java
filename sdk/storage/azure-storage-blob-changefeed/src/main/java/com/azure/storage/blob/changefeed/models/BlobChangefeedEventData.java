package com.azure.storage.blob.changefeed.models;

import com.azure.storage.blob.models.BlobType;
import org.apache.avro.generic.GenericRecord;

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

    public static BlobChangefeedEventData fromRecord(GenericRecord data) {
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

        return new BlobChangefeedEventData(api == null ? null : api.toString(),
            clientRequestId == null ? null : clientRequestId.toString(),
            requestId == null ? null : requestId.toString(),
            eTag == null ? null : eTag.toString(),
            contentType == null ? null : contentType.toString(),
            contentLength == null ? null : (Long) contentLength,
            blobType == null ? null : BlobType.fromString(blobType.toString()),
            contentOffset == null ? null : (Long) contentOffset,
            destinationUrl == null ? null : destinationUrl.toString(),
            sourceUrl == null ? null : sourceUrl.toString(),
            blobUrl == null ? null : blobUrl.toString(),
            recursive == null ? null : (Boolean) recursive,
            sequencer == null ? null : sequencer.toString());
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
}
