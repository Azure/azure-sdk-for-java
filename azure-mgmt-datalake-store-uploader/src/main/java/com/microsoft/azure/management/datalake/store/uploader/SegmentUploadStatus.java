/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

/**
 * Defines various states that a segment upload can have
 */
public enum SegmentUploadStatus {
    /**
    * Indicates that the segment is currently scheduled for upload.
    */
    Pending,

    /**
    * Indicates that the segment is currently being uploaded.
    */
    InProgress,

    /**
    * Indicates that the segment was not uploaded successfully.
    */
    Failed,

    /**
    * Indicates that the segment was successfully uploaded.
    */
    Complete
}
