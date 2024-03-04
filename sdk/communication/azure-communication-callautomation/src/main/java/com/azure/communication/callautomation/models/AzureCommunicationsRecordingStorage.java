// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The AzureBlobContainerRecordingStorage model. */
public class AzureCommunicationsRecordingStorage extends RecordingStorage {

    /*
     * Defines the kind of recording storage
     */
    @JsonProperty(value = "recordingStorageType", required = true)
    private RecordingStorageType recordingStorageType;

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
}
