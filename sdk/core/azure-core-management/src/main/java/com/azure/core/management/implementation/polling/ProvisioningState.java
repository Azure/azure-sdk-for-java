// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

/**
 * Represents the provisioning state of a ARM resource.
 */
final class ProvisioningState {
    /**
     * The provisioning state of resource if the operation is still in progress.
     */
    static final String IN_PROGRESS = "InProgress";

    /**
     * The provisioning state of resource if the operation is successful.
     */
    static final String SUCCEEDED = "Succeeded";

    /**
     * The provisioning state of resource if the operation is unsuccessful.
     */
    static final String FAILED = "Failed";

    /**
     * The provisioning state of resource if the operation is canceled.
     */
    static final String CANCELED = "Canceled";
}
