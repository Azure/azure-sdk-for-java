// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for BlobChangefeed.
 */
@Fluent
public class BlobChangefeedCursor {

    private ClientLogger logger = new ClientLogger(ChangefeedCursor.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .registerModule(new JavaTimeModule());

    @JsonProperty("CursorVersion")
    private int cursorVersion;

    @JsonProperty("UrlHash")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private byte[] urlHash;

    @JsonProperty("EndTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime endTime;

    @JsonProperty("CurrentSegmentCursor")
    private SegmentCursor currentSegmentCursor;

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public BlobChangefeedCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     */
    public BlobChangefeedCursor(int cursorVersion, byte[] urlHash, OffsetDateTime endTime, SegmentCursor currentSegmentCursor) {
        this.cursorVersion = cursorVersion;
        this.urlHash = urlHash;
        this.endTime = endTime;
        this.currentSegmentCursor = currentSegmentCursor;
    }

    /**
     * Creates a new changefeed level cursor with the specified end time.
     *
     * @param urlHash The url hash of the changefeed container.
     * @param endTime The {@link OffsetDateTime end time}.
     */
    public BlobChangefeedCursor(byte[] urlHash, OffsetDateTime endTime) {
        this(1, urlHash, endTime, null);
    }

    /**
     * Creates a new segment level cursor with the specified segment path.
     *
     * @param segmentPath The segment path.
     * @return A new segment level {@link BlobChangefeedCursor cursor}.
     */
    public BlobChangefeedCursor toSegmentCursor(String segmentPath) {
        return new BlobChangefeedCursor(this.cursorVersion, this.urlHash, this.endTime,
            new SegmentCursor(segmentPath, new ArrayList<>(), null));
    }

    /**
     * Creates a new shard level cursor with the specified shard path.
     *
     * @param shardPath The shard path.
     * @return A new shard level {@link BlobChangefeedCursor cursor}.
     */
    public BlobChangefeedCursor toShardCursor(String shardPath) {
        return new BlobChangefeedCursor(this.cursorVersion, this.urlHash, this.endTime,
            currentSegmentCursor.toShardCursor(shardPath));
    }

    /**
     * Creates a new event level cursor with the specified chunk path, block offset and event index.
     *
     * @param chunkPath The chunk path.
     * @param blockOffset The block offset.
     * @param eventIndex The event index.
     * @return A new event level {@link BlobChangefeedCursor cursor}.
     */
    public BlobChangefeedCursor toEventCursor(String chunkPath, long blockOffset, long eventIndex) {
        return new BlobChangefeedCursor(this.cursorVersion, this.urlHash, this.endTime,
            currentSegmentCursor.toEventCursor(chunkPath, blockOffset, eventIndex));
    }

    /**
     * @return the cursor version.
     */
    public int getCursorVersion() {
        return cursorVersion;
    }

    /**
     * @return the url hash.
     */
    public byte[] getUrlHash() {
        return urlHash.clone();
    }

    /**
     * @return the end time.
     */
    public OffsetDateTime getEndTime() {
        return endTime;
    }

    /**
     * @return the segment cursor.
     */
    public SegmentCursor getCurrentSegmentCursor() {
        return currentSegmentCursor;
    }

    /**
     * @param cursorVersion the cursor version.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setCursorVersion(int cursorVersion) {
        this.cursorVersion = cursorVersion;
        return this;
    }

    /**
     * @param urlHash the url hash.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setUrl(byte[] urlHash) {
        this.urlHash = urlHash.clone();
        return this;
    }

    /**
     * @param endTime the end time.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * @param currentSegmentCursor the segment cursor.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setCurrentSegmentCursor(SegmentCursor currentSegmentCursor) {
        this.currentSegmentCursor = currentSegmentCursor;
        return this;
    }

    /**
     * Serializes a {@link BlobChangefeedCursor} into a String.
     *
     * @return The resulting serialized cursor.
     */
    public String serialize() {
        try {
            return MAPPER.writer().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Deserializes a String into a {@link BlobChangefeedCursor}.
     *
     * @param cursor The cursor to deserialize.
     * @return The resulting {@link BlobChangefeedCursor cursor}.
     */
    public static BlobChangefeedCursor deserialize(String cursor, ClientLogger logger) {
        try {
            return MAPPER.readerFor(BlobChangefeedCursor.class).readValue(cursor);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
