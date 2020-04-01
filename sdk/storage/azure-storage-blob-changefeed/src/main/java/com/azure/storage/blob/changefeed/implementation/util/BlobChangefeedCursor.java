package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

public class BlobChangefeedCursor {

    private static final ClientLogger logger = new ClientLogger(BlobChangefeedCursor.class);

    private String endTime;
    private String segmentTime;
    private String shardPath;
    private String chunkPath;
    private Long eventIndex;
    private Boolean eventToBeProcessed;

    public BlobChangefeedCursor() {
    }

    public BlobChangefeedCursor(String endTime, String segmentTime, String shardPath, String chunkPath,
        Long eventIndex) {
        this.endTime = endTime;
        this.segmentTime = segmentTime;
        this.chunkPath = chunkPath;
        this.shardPath = shardPath;
        this.eventIndex = eventIndex;
    }

    public BlobChangefeedCursor toEventCursor(Long eventIndex) {
        return new BlobChangefeedCursor(this.getEndTime(), this.getSegmentTime(),
            this.getShardPath(), this.getChunkPath(), eventIndex);
    }

    public BlobChangefeedCursor toChunkCursor(String chunkPath) {
        return new BlobChangefeedCursor(this.getEndTime(), this.getSegmentTime(),
            this.getShardPath(), chunkPath, null);
    }

    public BlobChangefeedCursor toShardCursor(String shardPath) {
        return new BlobChangefeedCursor(this.getEndTime(), this.getSegmentTime(),
            shardPath, null, null);
    }

    public BlobChangefeedCursor toSegmentCursor(OffsetDateTime segmentTime) {
        return new BlobChangefeedCursor(this.getEndTime(), segmentTime.toString(),
            null, null, null);
    }

    public BlobChangefeedCursor(OffsetDateTime endTime) {
        this(endTime.toString(), null, null, null, null);
    }

    public String getEndTime() {
        return endTime;
    }

    public String getSegmentTime() {
        return segmentTime;
    }

    public String getChunkPath() {
        return chunkPath;
    }

    public String getShardPath() {
        return shardPath;
    }

    public Long getEventIndex() {
        return eventIndex;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setSegmentTime(String segmentTime) {
        this.segmentTime = segmentTime;
    }

    public void setShardPath(String shardPath) {
        this.shardPath = shardPath;
    }

    public void setChunkPath(String chunkPath) {
        this.chunkPath = chunkPath;
    }

    public void setEventIndex(Long eventIndex) {
        this.eventIndex = eventIndex;
    }

    public Boolean isEventToBeProcessed() {
        return eventToBeProcessed;
    }

    public void setEventToBeProcessed(Boolean eventToBeProcessed) {
        this.eventToBeProcessed = eventToBeProcessed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlobChangefeedCursor)) {
            return false;
        }

        BlobChangefeedCursor other = (BlobChangefeedCursor) obj;
        return other.getEndTime().equals(this.getEndTime())
            && other.getSegmentTime().equals(this.getSegmentTime())
            && other.getShardPath().equals(this.getShardPath())
            && other.getChunkPath().equals(this.getChunkPath())
            && other.getEventIndex().equals(this.getEventIndex());
    }

    public String serialize() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    public static BlobChangefeedCursor deserialize(String cursor) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(cursor, BlobChangefeedCursor.class);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
