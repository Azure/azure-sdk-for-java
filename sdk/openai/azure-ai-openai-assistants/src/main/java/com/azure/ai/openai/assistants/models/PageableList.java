// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * The response data for a requested list of items.
 *
 * @param <T> the type of the items in the list.
 */
@Immutable
public final class PageableList<T extends JsonSerializable<T>> implements JsonSerializable<PageableList<T>>  {

    /*
     * The object type, which is always list.
     */
    private String object = "list";

    /*
     * The requested list of items.
     */
    private List<T> data;

    /*
     * The first ID represented in this list.
     */
    private String firstId;

    /*
     * The last ID represented in this list.
     */
    private String lastId;

    /*
     * A value indicating whether there are additional values available not captured in this list.
     */
    private boolean hasMore;

    /**
     * Creates an instance of PageableList class.
     *
     * @param data the data value to set.
     * @param firstId the firstId value to set.
     * @param lastId the lastId value to set.
     * @param hasMore the hasMore value to set.
     */
    private PageableList(List<T> data, String firstId, String lastId, boolean hasMore) {
        this.data = data;
        this.firstId = firstId;
        this.lastId = lastId;
        this.hasMore = hasMore;
    }

    /**
     * Get the object property: The object type, which is always list.
     *
     * @return the object value.
     */
    public String getObject() {
        return this.object;
    }

    /**
     * Get the data property: The requested list of items.
     *
     * @return the data value.
     */
    public List<T> getData() {
        return this.data;
    }

    /**
     * Get the firstId property: The first ID represented in this list.
     *
     * @return the firstId value.
     */
    public String getFirstId() {
        return this.firstId;
    }

    /**
     * Get the lastId property: The last ID represented in this list.
     *
     * @return the lastId value.
     */
    public String getLastId() {
        return this.lastId;
    }

    /**
     * Get the hasMore property: A value indicating whether there are additional values available not captured in this
     * list.
     *
     * @return the hasMore value.
     */
    public boolean isHasMore() {
        return this.hasMore;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("object", this.object);
        jsonWriter.writeArrayField("data", this.data, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("first_id", this.firstId);
        jsonWriter.writeStringField("last_id", this.lastId);
        jsonWriter.writeBooleanField("has_more", this.hasMore);
        return jsonWriter;
    }

    public static <T extends JsonSerializable<T>> PageableList<T> fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<T> data = null;
            String firstId = null;
            String lastId = null;
            boolean hasMore = false;
            while(reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "data" -> data = reader.readArray(reader1 -> T.fromJson(reader1));
                    case "first_id" -> firstId = reader.getString();
                    case "last_id" -> lastId = reader.getString();
                    case "has_more" -> hasMore = reader.getBoolean();
                    case null, default -> reader.skipChildren();
                }
            }
            return new PageableList<>(data, firstId, lastId, hasMore);
        });
    }
}
