// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.models.RecordingStorageType;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

@Fluent
public abstract class ExternalStorage {
    @JsonProperty(value = "storageType")
    private RecordingStorageType storageType;

    public RecordingStorageType getStorageType() {
        return storageType;
    }

    protected ExternalStorage setStorageType(RecordingStorageType storageType) {
        this.storageType = storageType;
        return this;
    }
}
