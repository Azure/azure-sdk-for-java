/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

/**
 * Created by begoldsm on 4/12/2016.
 */
public enum SegmentUploadStatus {
    /// <summary>
    /// Indicates that the segment is currently scheduled for upload.
    /// </summary>
    Pending,

    /// <summary>
    /// Indicates that the segment is currently being uploaded.
    /// </summary>
    InProgress,

    /// <summary>
    /// Indicates that the segment was not uploaded successfully.
    /// </summary>
    Failed,

    /// <summary>
    /// Indicates that the segment was successfully uploaded.
    /// </summary>
    Complete
}
