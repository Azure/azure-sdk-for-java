// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for a shard in BlobChangefeed.
 */
@Fluent
public class ShardCursor implements JsonSerializable<ShardCursor> {
    private String currentChunkPath;
    private long blockOffset;
    private long eventIndex;

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public ShardCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     */
    public ShardCursor(String currentChunkPath, long blockOffset, long eventIndex) {
        this.currentChunkPath = currentChunkPath;
        this.blockOffset = blockOffset;
        this.eventIndex = eventIndex;
    }

    /**
     * @return the chunk path.
     */
    public String getCurrentChunkPath() {
        return currentChunkPath;
    }

    /**
     * @return the block offset.
     */
    public long getBlockOffset() {
        return blockOffset;
    }

    /**
     * @return the event index.
     */
    public long getEventIndex() {
        return eventIndex;
    }

    /**
     * @param currentChunkPath the chunk path.
     * @return the updated ShardCursor
     */
    public ShardCursor setCurrentChunkPath(String currentChunkPath) {
        this.currentChunkPath = currentChunkPath;
        return this;
    }

    /**
     * @param blockOffset the block offset.
     * @return the updated ShardCursor
     */
    public ShardCursor setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
        return this;
    }

    /**
     * @param eventIndex the event index.
     * @return the updated ShardCursor
     */
    public ShardCursor setEventIndex(long eventIndex) {
        this.eventIndex = eventIndex;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("CurrentChunkPath", currentChunkPath)
            .writeLongField("BlockOffset", blockOffset)
            .writeLongField("EventIndex", eventIndex)
            .writeEndObject();
    }

    /**
     * Deserialize a ShardCursor from JSON.
     *
     * @param jsonReader The JSON reader to deserialize from.
     * @return The deserialized ShardCursor.
     * @throws IOException If the JSON object cannot be properly deserialized.
     */
    public static ShardCursor fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ShardCursor shardCursor = new ShardCursor();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("CurrentChunkPath".equals(fieldName)) {
                    shardCursor.currentChunkPath = reader.getString();
                } else if ("BlockOffset".equals(fieldName)) {
                    shardCursor.blockOffset = reader.getLong();
                } else if ("EventIndex".equals(fieldName)) {
                    shardCursor.eventIndex = reader.getLong();
                } else {
                    reader.skipChildren();
                }
            }

            return shardCursor;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShardCursor)) {
            return false;
        }
        ShardCursor cursor = (ShardCursor) o;
        return getBlockOffset() == cursor.getBlockOffset()
            && getEventIndex() == cursor.getEventIndex()
            && Objects.equals(getCurrentChunkPath(), cursor.getCurrentChunkPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrentChunkPath(), getBlockOffset(), getEventIndex());
    }
}
