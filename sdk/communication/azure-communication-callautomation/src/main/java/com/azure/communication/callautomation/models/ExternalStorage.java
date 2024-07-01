// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** Abstract base class for different external storage types. */
@Fluent
public abstract class ExternalStorage implements JsonSerializable<ExternalStorage> {
    private final RecordingStorageType storageType;

    /**
     * Get the storageType property: Defines the type of external storage.
     *
     * @return the storageType value.
     */
    public RecordingStorageType getStorageType() {
        return storageType;
    }

    /**
     * The constructor
     *
     * @param storageType Specify the storage type kind.
     */
    ExternalStorage(RecordingStorageType storageType) {
        this.storageType = storageType;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        this.writeJsonImpl(jsonWriter);
        jsonWriter.writeStringField("storageType", this.storageType != null ? this.storageType.toString() : null);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ExternalStorage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ExternalStorage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ExternalStorage.
     */
    public static ExternalStorage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String type = null;
            final JsonReader reader1 = reader.bufferObject();
            reader1.nextToken(); // Prepare for reading
            while (reader1.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader1.getFieldName();
                reader1.nextToken();
                if ("storageType".equals(fieldName)) {
                    type = reader1.getString();
                } else {
                    reader1.skipChildren();
                }
            }
            final ExternalStorage storage;
            if ("blobStorage".equals(type)) {
                storage = BlobStorage.readJsonImpl(reader1.reset());
            } else {
                storage = null;
            }
            return storage;
        });
    }

    abstract void writeJsonImpl(JsonWriter jsonWriter) throws IOException;
}
