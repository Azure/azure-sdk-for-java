// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for RecordingStorageType. */
public final class RecordingStorageType extends ExpandableStringEnum<RecordingStorageType> {

    /** Static value acs for RecordingStorageTypeInternal. */
    public static final RecordingStorageType ACS = fromString("acs");

    /** Static value blobStorage for RecordingStorageTypeInternal. */
    public static final RecordingStorageType BLOB_STORAGE = fromString("blobStorage");

    /**
     * Creates or finds a RecordingStorageTypeInternal from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecordingStorageTypeInternal.
     */
    public static RecordingStorageType fromString(String name) {
        return fromString(name, RecordingStorageType.class);
    }

    /**
     * Gets known RecordingStorageTypeInternal values.
     *
     * @return known RecordingStorageTypeInternal values.
     */
    public static Collection<RecordingStorageType> values() {
        return values(RecordingStorageType.class);
    }
}
