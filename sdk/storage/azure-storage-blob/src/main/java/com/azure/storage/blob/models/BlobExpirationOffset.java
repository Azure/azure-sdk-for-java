// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Enum to specify when a blob's expiration time should be relative to.
 */
public enum BlobExpirationOffset {
    /**
     * Blob's expiration time should be set relative to the blob creation time.
     */
    CREATION_TIME,
    /**
     * Blob's expiration time should be set relative to the current time.
     */
    NOW
}
