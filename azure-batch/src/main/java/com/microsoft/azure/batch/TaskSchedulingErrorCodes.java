/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

// Contains error codes specific to task scheduling errors.
public final class TaskSchedulingErrorCodes
{
    // Access was denied when trying to download a resource file required for the task.
    public static final String BlobAccessDenied = "BlobAccessDenied";

    // An error occurred when trying to download a resource file required for the task.
    public static final String BlobDownloadMiscError = "BlobDownloadMiscError";

    // A timeout occurred when downloading a resource file required for the task.
    public static final String BlobDownloadTimedOut = "BlobDownloadTimedOut";

    // A resource file required for the task does not exist.
    public static final String BlobNotFound = "BlobNotFound";

    // An error occurred when launching the task's command line.
    public static final String CommandLaunchFailed = "CommandLaunchFailed";

    // The program specified in the task's command line was not found.
    public static final String CommandProgramNotFound = "CommandProgramNotFound";

    // The compute node disk ran out of space when downloading the resource files required for the task.
    public static final String DiskFull = "DiskFull";

    // The compute node could not create a directory for the task's resource files.
    public static final String ResourceDirectoryCreateFailed = "ResourceDirectoryCreateFailed";

    // The compute node could not create a local file when trying to download a resource file required for the task.
    public static final String ResourceFileCreateFailed = "ResourceFileCreateFailed";

    // The compute node could not write to a local file when trying to download a resource file required for the task.
    public static final String ResourceFileWriteFailed = "ResourceFileWriteFailed";

    // The task ended.
    public static final String TaskEnded = "TaskEnded";

    // The reason for the scheduling error is unknown.
    public static final String Unknown = "Unknown";

}
