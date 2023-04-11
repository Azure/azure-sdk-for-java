// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Abstract base class for different external storage types. */
@Fluent
public abstract class ExternalStorage {
    @JsonProperty(value = "storageType")
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
}
