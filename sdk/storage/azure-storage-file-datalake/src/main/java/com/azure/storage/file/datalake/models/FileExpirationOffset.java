// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Enum to specify when a file's expiration time should be relative to.
 */
public enum FileExpirationOffset {
    /**
     * Files's expiration time should be set relative to the file creation time.
     */
    CREATION_TIME,
    /**
     * Files's expiration time should be set relative to the current time.
     */
    NOW
}
