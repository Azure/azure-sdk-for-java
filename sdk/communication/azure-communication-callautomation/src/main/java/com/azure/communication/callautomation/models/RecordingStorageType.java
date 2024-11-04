// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for RecordingStorageType. */
public final class RecordingStorageType extends ExpandableStringEnum<RecordingStorageType> {

    /** Static value acs for RecordingStorageType. */
    public static final RecordingStorageType ACS = fromString("acs");

    /** Static value azureblobStorage for RecordingStorageType. */
    public static final RecordingStorageType AZURE_BLOB_STORAGE = fromString("blobStorage");

    /**
     * Creates or finds a RecordingStorageType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecordingStorageType.
     */
    public static RecordingStorageType fromString(String name) {
        return fromString(name, RecordingStorageType.class);
    }

    /**
     * Gets known RecordingStorageType values.
     *
     * @return known RecordingStorageType values.
     */
    public static Collection<RecordingStorageType> values() {
        return values(RecordingStorageType.class);
    }
}
