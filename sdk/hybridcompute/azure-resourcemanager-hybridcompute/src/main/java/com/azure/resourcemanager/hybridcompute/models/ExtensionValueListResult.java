// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridcompute.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.hybridcompute.fluent.models.ExtensionValueInner;
import java.io.IOException;
import java.util.List;

/**
 * The List Extension Metadata response.
 */
@Immutable
public final class ExtensionValueListResult implements JsonSerializable<ExtensionValueListResult> {
    /*
     * The list of extension metadata
     */
    private List<ExtensionValueInner> value;

    /**
     * Creates an instance of ExtensionValueListResult class.
     */
    public ExtensionValueListResult() {
    }

    /**
     * Get the value property: The list of extension metadata.
     * 
     * @return the value value.
     */
    public List<ExtensionValueInner> value() {
        return this.value;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() != null) {
            value().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ExtensionValueListResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ExtensionValueListResult if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ExtensionValueListResult.
     */
    public static ExtensionValueListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ExtensionValueListResult deserializedExtensionValueListResult = new ExtensionValueListResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<ExtensionValueInner> value
                        = reader.readArray(reader1 -> ExtensionValueInner.fromJson(reader1));
                    deserializedExtensionValueListResult.value = value;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedExtensionValueListResult;
        });
    }
}
