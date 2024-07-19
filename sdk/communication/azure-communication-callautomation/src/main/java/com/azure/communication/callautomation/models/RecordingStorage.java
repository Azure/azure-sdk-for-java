// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;

/** The RecordingStorage model. */
@Fluent
public abstract class RecordingStorage implements JsonSerializable<RecordingStorage> {

    /** Creates an instance of ExternalStorageInternal class. */
    public RecordingStorage() {}

    /**
     * Get the recordingStorageType property: Defines the kind of external storage.
     *
     * @return the recordingStorageType value.
     */
    public abstract RecordingStorageType getRecordingStorageType();
}
