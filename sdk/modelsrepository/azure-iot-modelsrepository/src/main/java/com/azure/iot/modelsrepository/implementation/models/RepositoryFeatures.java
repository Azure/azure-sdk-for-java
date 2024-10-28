// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * {@link RepositoryFeatures} is designated to store
 * attributes which apply to all models in the repository
 */
public class RepositoryFeatures implements JsonSerializable<RepositoryFeatures> {
    private final boolean expanded;
    private final boolean index;

    public RepositoryFeatures(boolean expanded, boolean index) {
        this.expanded = expanded;
        this.index = index;
    }

    public RepositoryFeatures() {
        this.expanded = false;
        this.index = false;
    }

    public boolean isIndex() {
        return index;
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeBooleanField("expanded", expanded)
            .writeBooleanField("index", index)
            .writeEndObject();
    }

    public static RepositoryFeatures fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            boolean expanded = false;
            boolean index = false;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("expanded".equals(fieldName)) {
                    expanded = reader.getBoolean();
                } else if ("index".equals(fieldName)) {
                    index = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }

            return new RepositoryFeatures(expanded, index);
        });
    }
}
