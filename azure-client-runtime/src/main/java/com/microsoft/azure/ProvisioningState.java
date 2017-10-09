/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

/**
 * The different provisioning states that a long running operation can be in.
 */
public final class ProvisioningState {
    /**
     * The provisioning state of the operation resource if the operation is still in progress.
     */
    public static final String IN_PROGRESS = "InProgress";

    /**
     * The provisioning state of the operation resource if the operation is successful.
     */
    public static final String SUCCEEDED = "Succeeded";

    /**
     * The provisioning state of the operation resource if the operation is unsuccessful.
     */
    public static final String FAILED = "Failed";

    public static boolean isCompleted(String provisioningState) {
        return SUCCEEDED.equalsIgnoreCase(provisioningState) || FAILED.equalsIgnoreCase(provisioningState);
    }
}
