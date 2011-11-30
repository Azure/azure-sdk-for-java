/**
 * 
 */
package com.microsoft.windowsazure.services.blob.client;

/**
 * Specifies options when calling delete operations.
 */
public enum DeleteSnapshotsOption {
    /**
     * Specifies deleting only the blob's snapshots.
     */
    DELETE_SNAPSHOTS_ONLY,

    /**
     * Specifies deleting the blob and its snapshots.
     */
    INCLUDE_SNAPSHOTS,

    /**
     * Specifies deleting the blob but not its snapshots.
     */
    NONE
}
