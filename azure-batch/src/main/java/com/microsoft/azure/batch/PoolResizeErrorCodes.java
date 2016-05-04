/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

// Contains error codes specific to pool resize errors.
public final class PoolResizeErrorCodes
{
    // The account has reached its quota of compute nodes.
    public static final String AccountCoreQuotaReached = "AccountCoreQuotaReached";

    // An error occurred while trying to allocate the desired number of compute nodes.
    public static final String AllocationFailed = "AllocationFailed";

    // The Batch service was unable to allocate the desired number of compute nodes within the resize timeout.
    public static final String AllocationTimedout= "AllocationTimedout";

    // An error occurred when removing compute nodes from the pool.
    public static final String RemoveNodesFailed = "RemoveNodesFailed";

    // The user stopped the resize operation.
    public static final String ResizeStopped = "ResizeStopped";

    // The reason for the failure is not known.
    public static final String Unknown = "Unknown";
}
