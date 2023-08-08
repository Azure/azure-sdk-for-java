// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

/**
 * Defines values for ShareSnapshotsDeleteOptionType.
 */
public enum ShareSnapshotsDeleteOptionType {
    /**
     * The share's snapshots that do not have an active lease will be deleted with the share.
     */
    INCLUDE,

    /**
     *  All of the share's snapshots, including those with an active lease, will be deleted with the share.
     */
    INCLUDE_WITH_LEASED;
}
