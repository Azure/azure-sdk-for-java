// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.models.RecordingStorageType;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Abstract base class for different external storage types. */
@Fluent
public abstract class ExternalStorage {
    @JsonProperty(value = "storageType")
    private RecordingStorageType storageType;

    /**
     * Get the storageType property: Defines the type of external storage.
     *
     * @return the storageType value.
     */
    public RecordingStorageType getStorageType() {
        return storageType;
    }

    /**
     * Set the storageType property: Defines the type of external storage.
     *
     * @param storageType the storageType value to set.
     * @return the ExternalStorage object itself.
     */
    protected ExternalStorage setStorageType(RecordingStorageType storageType) {
        this.storageType = storageType;
        return this;
    }
}
