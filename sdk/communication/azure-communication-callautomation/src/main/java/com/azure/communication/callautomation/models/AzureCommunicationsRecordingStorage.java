// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The AzureBlobContainerRecordingStorage model. */
public class AzureCommunicationsRecordingStorage extends RecordingStorage {

    /*
     * Defines the kind of recording storage
     */
    private final RecordingStorageType recordingStorageType;

    /**
     * Creates an instance of AzureBlobContainerRecordingStorage class.
     */
    public AzureCommunicationsRecordingStorage() {
        this.recordingStorageType = RecordingStorageType.fromString("AzureCommunicationServices");
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
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AzureCommunicationsRecordingStorage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AzureCommunicationsRecordingStorage if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AzureCommunicationsRecordingStorage.
     */
    public static AzureCommunicationsRecordingStorage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            return new AzureCommunicationsRecordingStorage();
        });
    }
}
