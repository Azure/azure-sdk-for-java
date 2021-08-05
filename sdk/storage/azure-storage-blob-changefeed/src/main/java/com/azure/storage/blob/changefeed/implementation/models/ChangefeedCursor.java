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
import java.util.Objects;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for BlobChangefeed.
 */
@Fluent
public class ChangefeedCursor {

    private ClientLogger logger = new ClientLogger(ChangefeedCursor.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .registerModule(new JavaTimeModule());

    @JsonProperty("CursorVersion")
    private int cursorVersion;

    @JsonProperty("UrlHost")
    private String urlHost;

    @JsonProperty("EndTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime endTime;

    @JsonProperty("CurrentSegmentCursor")
    private SegmentCursor currentSegmentCursor;

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public ChangefeedCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     */
    public ChangefeedCursor(int cursorVersion, String urlHost, OffsetDateTime endTime, SegmentCursor currentSegmentCursor) {
        this.cursorVersion = cursorVersion;
        this.urlHost = urlHost;
        this.endTime = endTime;
        this.currentSegmentCursor = currentSegmentCursor;
    }

    /**
     * Creates a new changefeed level cursor with the specified end time.
     *
     * @param urlHost The url host of the changefeed container.
     * @param endTime The {@link OffsetDateTime end time}.
     */
    public ChangefeedCursor(String urlHost, OffsetDateTime endTime) {
        this(1, urlHost, endTime, null);
    }

    /**
     * Creates a new segment level cursor with the specified segment path.
     *
     * @param segmentPath The segment path.
     * @param userSegmentCursor The user segment cursor (Used to populate the list of shard cursors).
     * @return A new segment level {@link ChangefeedCursor cursor}.
     */
    public ChangefeedCursor toSegmentCursor(String segmentPath, SegmentCursor userSegmentCursor) {
        return new ChangefeedCursor(this.cursorVersion, this.urlHost, this.endTime,
            new SegmentCursor(segmentPath, userSegmentCursor));
    }

    /**
     * Creates a new shard level cursor with the specified shard path.
     *
     * @param shardPath The shard path.
     * @return A new shard level {@link ChangefeedCursor cursor}.
     */
    public ChangefeedCursor toShardCursor(String shardPath) {
        return new ChangefeedCursor(this.cursorVersion, this.urlHost, this.endTime,
            currentSegmentCursor.toShardCursor(shardPath));
    }

    /**
     * Creates a new event level cursor with the specified chunk path, block offset and event index.
     *
     * @param chunkPath The chunk path.
     * @param blockOffset The block offset.
     * @param eventIndex The event index.
     * @return A new event level {@link ChangefeedCursor cursor}.
     */
    public ChangefeedCursor toEventCursor(String chunkPath, long blockOffset, long eventIndex) {
        return new ChangefeedCursor(this.cursorVersion, this.urlHost, this.endTime,
            currentSegmentCursor.toEventCursor(chunkPath, blockOffset, eventIndex));
    }

    /**
     * @return the cursor version.
     */
    public int getCursorVersion() {
        return cursorVersion;
    }

    /**
     * @return the url host.
     */
    public String getUrlHost() {
        return urlHost;
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
    public ChangefeedCursor setCursorVersion(int cursorVersion) {
        this.cursorVersion = cursorVersion;
        return this;
    }

    /**
     * @param urlHost the url hash.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setUrlHost(String urlHost) {
        this.urlHost = urlHost;
        return this;
    }

    /**
     * @param endTime the end time.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * @param currentSegmentCursor the segment cursor.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setCurrentSegmentCursor(SegmentCursor currentSegmentCursor) {
        this.currentSegmentCursor = currentSegmentCursor;
        return this;
    }

    /**
     * Serializes a {@link ChangefeedCursor} into a String.
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
     * Deserializes a String into a {@link ChangefeedCursor}.
     *
     * @param cursor The cursor to deserialize.
     * @return The resulting {@link ChangefeedCursor cursor}.
     */
    public static ChangefeedCursor deserialize(String cursor, ClientLogger logger) {
        try {
            return MAPPER.readerFor(ChangefeedCursor.class).readValue(cursor);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChangefeedCursor)) {
            return false;
        }
        ChangefeedCursor cursor = (ChangefeedCursor) o;
        return getCursorVersion() == cursor.getCursorVersion()
            && Objects.equals(getUrlHost(), cursor.getUrlHost())
            && Objects.equals(getEndTime(), cursor.getEndTime())
            && Objects.equals(getCurrentSegmentCursor(), cursor.getCurrentSegmentCursor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCursorVersion(), getUrlHost(), getEndTime(), getCurrentSegmentCursor());
    }
}
