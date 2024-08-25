// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The AzureCommunicationRecordingStorage model. */
public class AzureBlobContainerRecordingStorage extends RecordingStorage {

    /*
     * Defines the kind of recording storage
     */
    @JsonProperty(value = "recordingStorageType", required = true)
    private RecordingStorageType recordingStorageType;

    /*
     * Uri of a container or a location within a container
     */
    @JsonProperty(value = "recordingDestinationContainerUrl")
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
}
