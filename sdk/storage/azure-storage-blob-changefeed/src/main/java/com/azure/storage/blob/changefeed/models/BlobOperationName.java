// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * This class represents the operation name that triggered a {@link BlobChangefeedEvent}.
 */
public final class BlobOperationName extends ExpandableStringEnum<BlobOperationName> {

    /** Static value AppendBlock for BlobOperationName. */
    public static final BlobOperationName APPEND_BLOCK = fromString("AppendBlock");

    /** Static value UpdateLastAccessTime for BlobOperationName. */
    public static final BlobOperationName UPDATE_LAST_ACCESS_TIME = fromString("UpdateLastAccessTime");

    /** Static value CreateContainer for BlobOperationName. */
    public static final BlobOperationName CREATE_CONTAINER = fromString("ContainerCreated");

    /** Static value DeleteContainer for BlobOperationName. */
    public static final BlobOperationName DELETE_CONTAINER = fromString("ContainerDeleted");

    /** Static value RestoreContainer for BlobOperationName. */
    public static final BlobOperationName RESTORE_CONTAINER = fromString("RestoreContainer");

    /** Static value SetContainerMetadata for BlobOperationName. */
    public static final BlobOperationName SET_CONTAINER_METADATA = fromString("SetContainerMetadata");

    /**
     * Creates a new instance of {@link BlobOperationName} with no string value.
     *
     * @deprecated Please use {@link #fromString(String)} to create an instance of BlobOperationName.
     */
    @Deprecated
    public BlobOperationName() {
    }

    /**
     * Creates or finds a BlobOperationName from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding BlobOperationName.
     */
    public static BlobOperationName fromString(String name) {
        return fromString(name, BlobOperationName.class);
    }

    /**
     * Gets known BlobOperationName values.
     *
     * @return known BlobOperationName values.
     */
    public static Collection<BlobOperationName> values() {
        return values(BlobOperationName.class);
    }
}
