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
    protected String recordingDestinationContainerUrl;

    /** Creates an instance of ExternalStorageInternal class. */
    public RecordingStorage() {}

    /**
     * Get the recordingStorageType property: Defines the kind of external storage.
     *
     * @return the recordingStorageType value.
     */
    public abstract RecordingStorageType getRecordingStorageType();

    /**
     * Set the recordingStorageType property: Defines the kind of external storage.
     *
     * @param recordingStorageType the recordingStorageType value to set.
     * @return the ExternalStorageInternal object itself.
     */
    protected RecordingStorage setRecordingStorageType(RecordingStorageType recordingStorageType) {
        this.recordingStorageType = recordingStorageType;
        return this;
    }
}
