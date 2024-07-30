// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for a segment in BlobChangefeed.
 */
@Fluent
public class SegmentCursor implements JsonSerializable<SegmentCursor> {
    private List<ShardCursor> shardCursors;
    private String currentShardPath; // 'log/00/2020/07/06/1600/'
    private String segmentPath; //  'idx/segments/2020/07/06/1600/meta.json'

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public SegmentCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     */
    public SegmentCursor(String segmentPath, List<ShardCursor> shardCursors, String currentShardPath) {
        this.segmentPath = segmentPath;
        this.shardCursors = shardCursors;
        this.currentShardPath = currentShardPath;
    }

    /**
     * Creates a new segment level cursor with the specified segment path.
     *
     * @param segmentPath The segment path.
     * @param userSegmentCursor The user segment cursor (Used to populate the list of shard cursors).
     */
    public SegmentCursor(String segmentPath, SegmentCursor userSegmentCursor) {
        this.segmentPath = segmentPath;
        /* Deep copy the user segment cursor's list of shard cursors to make a new segment cursor. */
        /* We need to do this since a shard cursor could sit at the end of a shard, in which case an event will not
        *  be emitted, but we need to retain it in the list of shard cursors since the absence of a shard cursor
        *  indicates we need to start from the beginning of a shard. */
        List<ShardCursor> copy = new ArrayList<>();
        if (userSegmentCursor != null) {
            userSegmentCursor.getShardCursors()
                .forEach(shardCursor ->
                    copy.add(new ShardCursor(shardCursor.getCurrentChunkPath(), shardCursor.getBlockOffset(),
                        shardCursor.getEventIndex())));
        }
        this.shardCursors = copy;
        this.currentShardPath = null;
    }

    /**
     * Creates a new shard level cursor with the specified shard path.
     *
     * @param shardPath The shard path.
     * @return A new shard level {@link SegmentCursor cursor}.
     */
    public SegmentCursor toShardCursor(String shardPath) {
        /* Not cloning shard cursors list so we save state within the segment level. */
        return new SegmentCursor(this.segmentPath, this.shardCursors, shardPath);
    }

    /**
     * Creates a new event level cursor with the specified chunk path, block offset and event index.
     *
     * @param chunkPath The chunk path.
     * @param blockOffset The block offset.
     * @param eventIndex The event index.
     * @return A new event level {@link SegmentCursor cursor}.
     */
    public SegmentCursor toEventCursor(String chunkPath, long blockOffset, long eventIndex) {
        /* Deep copy the list to attach to the event. */
        List<ShardCursor> copy = new ArrayList<>(this.shardCursors.size() + 1);

        boolean found = false; /* Whether or not this shardPath exists in the list. */
        for (ShardCursor cursor : this.shardCursors) {
            /* If we found a shard cursor for this shard, modify it. */
            if (cursor.getCurrentChunkPath().contains(this.currentShardPath)) {
                found = true;
                cursor
                    .setCurrentChunkPath(chunkPath)
                    .setBlockOffset(blockOffset)
                    .setEventIndex(eventIndex);
            }
            /* Add the cursor to the copied list after modifying it. */
            copy.add(new ShardCursor(cursor.getCurrentChunkPath(), cursor.getBlockOffset(), cursor.getEventIndex()));
        }

        /* If a shard cursor for this shard does not exist in the list, add it,
           and add it to the copied list as well. */
        if (!found) {
            this.shardCursors.add(new ShardCursor(chunkPath, blockOffset, eventIndex));
            copy.add(new ShardCursor(chunkPath, blockOffset, eventIndex));
        }

        return new SegmentCursor(this.segmentPath, copy, this.currentShardPath);
    }

    /**
     * @return the segment path.
     */
    public String getSegmentPath() {
        return segmentPath;
    }

    /**
     * @return the shard cursors.
     */
    public List<ShardCursor> getShardCursors() {
        return shardCursors;
    }

    /**
     * @return the shard path.
     */
    public String getCurrentShardPath() {
        return currentShardPath;
    }

    /**
     * @param segmentPath the segment path.
     * @return the updated SegmentCursor
     */
    public SegmentCursor setSegmentPath(String segmentPath) {
        this.segmentPath = segmentPath;
        return this;
    }

    /**
     * @param shardCursors the shard cursors.
     * @return the updated SegmentCursor
     */
    public SegmentCursor setShardCursors(List<ShardCursor> shardCursors) {
        this.shardCursors = shardCursors;
        return this;
    }

    /**
     * @param currentShardPath the shard path.
     * @return the updated SegmentCursor
     */
    public SegmentCursor setCurrentShardPath(String currentShardPath) {
        this.currentShardPath = currentShardPath;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeArrayField("ShardCursors", shardCursors, JsonWriter::writeJson)
            .writeStringField("CurrentShardPath", currentShardPath)
            .writeStringField("SegmentPath", segmentPath)
            .writeEndObject();
    }

    /**
     * Deserialize a SegmentCursor from JSON.
     *
     * @param jsonReader The JSON reader to deserialize from.
     * @return The deserialized SegmentCursor.
     * @throws IOException If the JSON object cannot be properly deserialized.
     */
    public static SegmentCursor fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SegmentCursor segmentCursor = new SegmentCursor();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("ShardCursors".equals(fieldName)) {
                    segmentCursor.shardCursors = reader.readArray(ShardCursor::fromJson);
                } else if ("CurrentShardPath".equals(fieldName)) {
                    segmentCursor.currentShardPath = reader.getString();
                } else if ("SegmentPath".equals(fieldName)) {
                    segmentCursor.segmentPath = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return segmentCursor;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SegmentCursor)) {
            return false;
        }
        SegmentCursor that = (SegmentCursor) o;
        return Objects.equals(getShardCursors(), that.getShardCursors())
            && Objects.equals(getCurrentShardPath(), that.getCurrentShardPath())
            && Objects.equals(getSegmentPath(), that.getSegmentPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getShardCursors(), getCurrentShardPath(), getSegmentPath());
    }
}
