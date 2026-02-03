// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation.models;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * The result of a list request.
 */
public final class FooListResult implements JsonSerializable<FooListResult> {
    /*
     * The collection value.
     */
    private List<Foo> items;

    /*
     * The URI that can be used to request the next set of paged results.
     */
    private String nextLink;

    /**
     * Creates an instance of FooListResult class.
     */
    public FooListResult() {
    }

    /**
     * Get the items property: The collection value.
     *
     * @return the items value.
     */
    public List<Foo> getItems() {
        return this.items;
    }

    /**
     * Set the items property: The collection value.
     *
     * @param items the items value to set.
     * @return the FooListResult object itself.
     */
    public FooListResult setItems(List<Foo> items) {
        this.items = items;
        return this;
    }

    /**
     * Get the nextLink property: The URI that can be used to request the next set of paged results.
     *
     * @return the nextLink value.
     */
    public String getNextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The URI that can be used to request the next set of paged results.
     *
     * @param nextLink the nextLink value to set.
     * @return the FooListResult object itself.
     */
    public FooListResult setNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("items", this.items, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("@nextLink", this.nextLink);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of FooListResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of FooListResult if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the FooListResult.
     */
    public static FooListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FooListResult deserializedFooListResult = new FooListResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("items".equals(fieldName)) {
                    List<Foo> items = reader.readArray(reader1 -> Foo.fromJson(reader1));
                    deserializedFooListResult.items = items;
                } else if ("@nextLink".equals(fieldName)) {
                    deserializedFooListResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedFooListResult;
        });
    }
}
