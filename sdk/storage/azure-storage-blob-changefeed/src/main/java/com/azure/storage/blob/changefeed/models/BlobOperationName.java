// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * This class represents the operation name that triggered a {@link BlobChangefeedEvent}.
 */
public final class BlobOperationName extends ExpandableStringEnum<BlobOperationName> {

    /** Static value UnspecifiedApi for BlobOperationName. */
    public static final BlobOperationName UNSPECIFIED_API = fromString("UnspecifiedApi");

    /** Static value PutBlob for BlobOperationName. */
    public static final BlobOperationName PUT_BLOB = fromString("PutBlob");

    /** Static value PutBlockList for BlobOperationName. */
    public static final BlobOperationName PUT_BLOCK_LIST = fromString("PutBlockList");

    /** Static value CopyBlob for BlobOperationName. */
    public static final BlobOperationName COPY_BLOB = fromString("CopyBlob");

    /** Static value DeleteBlob for BlobOperationName. */
    public static final BlobOperationName DELETE_BLOB = fromString("DeleteBlob");

    /** Static value SetBlobMetadata for BlobOperationName. */
    public static final BlobOperationName SET_BLOB_METADATA = fromString("SetBlobMetadata");

    /** Static value ControlEvent for BlobOperationName. */
    public static final BlobOperationName CONTROL_EVENT = fromString("ControlEvent");

    /** Static value UndeleteBlob for BlobOperationName. */
    public static final BlobOperationName UNDELETE_BLOB = fromString("UndeleteBlob");

    /** Static value SetBlobProperties for BlobOperationName. */
    public static final BlobOperationName SET_BLOB_PROPERTIES = fromString("SetBlobProperties");

    /** Static value SnapshotBlob for BlobOperationName. */
    public static final BlobOperationName SNAPSHOT_BLOB = fromString("SnapshotBlob");

    /** Static value SetBlobTier for BlobOperationName. */
    public static final BlobOperationName SET_BLOB_TIER = fromString("SetBlobTier");

    /** Static value AbortCopyBlob for BlobOperationName. */
    public static final BlobOperationName ABORT_COPY_BLOB = fromString("AbortCopyBlob");

    /** Static value SetBlobTags for BlobOperationName. */
    public static final BlobOperationName SET_BLOB_TAGS = fromString("SetBlobTags");

    /** Static value CreateRestorePointMarker for BlobOperationName. */
    public static final BlobOperationName CREATE_RESTORE_POINT_MARKER = fromString("CreateRestorePointMarker");

    /** Static value AppendBlock for BlobOperationName. Schema V6. */
    public static final BlobOperationName APPEND_BLOCK = fromString("AppendBlock");

    /** Static value UpdateLastAccessTime for BlobOperationName. Schema V7. */
    public static final BlobOperationName UPDATE_LAST_ACCESS_TIME = fromString("UpdateLastAccessTime");

    /** Static value CreateContainer for BlobOperationName. Schema V8. */
    public static final BlobOperationName CREATE_CONTAINER = fromString("ContainerCreated");

    /** Static value DeleteContainer for BlobOperationName. Schema V8. */
    public static final BlobOperationName DELETE_CONTAINER = fromString("ContainerDeleted");

    /** Static value RestoreContainer for BlobOperationName. Schema V8. */
    public static final BlobOperationName RESTORE_CONTAINER = fromString("RestoreContainer");

    /** Static value SetContainerMetadata for BlobOperationName. Schema V8. */
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
