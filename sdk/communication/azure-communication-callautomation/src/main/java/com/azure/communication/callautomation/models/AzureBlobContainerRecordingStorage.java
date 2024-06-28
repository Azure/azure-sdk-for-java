// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The AzureCommunicationRecordingStorage model. */
public class AzureBlobContainerRecordingStorage extends RecordingStorage {

    /*
     * Defines the kind of recording storage
     */
    private final RecordingStorageType recordingStorageType;

    /*
     * Uri of a container or a location within a container
     */
    private String recordingDestinationContainerUrl;

    /** Creates an instance of AzureCommunicationRecordingStorage class.
     *
     * @param recordingDestinationContainerUrl the recordingDestinationContainerUrl value to set.
     */
    public AzureBlobContainerRecordingStorage(String recordingDestinationContainerUrl) {
        this.recordingStorageType = RecordingStorageType.fromString("AzureBlobStorage");
        this.recordingDestinationContainerUrl = recordingDestinationContainerUrl;
    }

    /**
     * Get the recordingDestinationContainerUrl property: Uri of a container or a location within a container.
     *
     * @return the recordingDestinationContainerUrl value.
     */
    public String getRecordingDestinationContainerUrl() {
        return this.recordingDestinationContainerUrl;
    }

    /**
     * Get the recordingStorageType property: Defines the kind of external storage.
     *
     * @return the recordingStorageType value.
     */
    @Override
    public RecordingStorageType getRecordingStorageType() {
        return this.recordingStorageType;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("recordingStorageType", recordingStorageType != null ? recordingStorageType.toString() : null);
        jsonWriter.writeStringField("recordingDestinationContainerUrl", recordingDestinationContainerUrl);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AzureBlobContainerRecordingStorage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AzureBlobContainerRecordingStorage if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AzureBlobContainerRecordingStorage.
     */
    public static AzureBlobContainerRecordingStorage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String recordingDestinationContainerUrl = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("recordingDestinationContainerUrl".equals(fieldName)) {
                    recordingDestinationContainerUrl = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new AzureBlobContainerRecordingStorage(recordingDestinationContainerUrl);
        });
    }
}
