// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The RecordingStorage model. */
@Fluent
public abstract class RecordingStorage {
    /*
     * Defines the kind of recording storage
     */
    @JsonProperty(value = "recordingStorageType", required = true)
    protected RecordingStorageType recordingStorageType;

    /*
     * Uri of a container or a location within a container
     */
    @JsonProperty(value = "recordingDestinationContainerUrl")
    private String recordingDestinationContainerUrl;

    /** Creates an instance of ExternalStorageInternal class. */
    public RecordingStorage() {}

    /**
     * Get the recordingStorageType property: Defines the kind of external storage.
     *
     * @return the recordingStorageType value.
     */
    public RecordingStorageType getRecordingStorageType() {
        return this.recordingStorageType;
    }

    /**
     * Set the recordingStorageType property: Defines the kind of external storage.
     *
     * @param recordingStorageType the recordingStorageType value to set.
     * @return the ExternalStorageInternal object itself.
     */
    public RecordingStorage setRecordingStorageType(RecordingStorageType recordingStorageType) {
        this.recordingStorageType = recordingStorageType;
        return this;
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
     * Set the recordingDestinationContainerUrl property: Uri of a container or a location within a container.
     *
     * @param recordingDestinationContainerUrl the recordingDestinationContainerUrl value to set.
     * @return the ExternalStorageInternal object itself.
     */
    public RecordingStorage setRecordingDestinationContainerUrl(String recordingDestinationContainerUrl) {
        this.recordingDestinationContainerUrl = recordingDestinationContainerUrl;
        return this;
    }
    
    /**
    * Creates AzureBlobContainer Storage for Recording.
    *
    * @return new AzureBlobContainerRecordingStorage object.
    */
    public static RecordingStorage createAzureBlobContainerRecordingStorage(String recordingDestinationContainerUri) {
        return new AzureBlobContainerRecordingStorage(recordingDestinationContainerUri);
    }

    /**
     * Creates AzureCommunications Storage for Recording.
     * 
     * @return new AzureCommunicationsRecordingStorage object.
     */
    public static RecordingStorage createAzureCommunicationsRecordingStorage() {
        return new AzureCommunicationsRecordingStorage();
    }
}
