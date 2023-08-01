// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

/**
 * The various long-running-operation polling types.
 */
enum PollingType {
    /**
     * Azure-AsyncOperation header based polling.
     */
    AZURE_ASYNC_OPERATION_POLL,
    /**
     * Location header based polling.
     */
    LOCATION_POLL,
    /**
     * Resource provisioning state based polling.
     */
    PROVISIONING_STATE_POLL,
    /**
     * Indicates polling not necessary as LRO succeeded synchronously.
     */
    SYNCHRONOUSLY_SUCCEEDED_LRO_NO_POLL,
    /**
     * Indicates no polling necessary as LRO failed synchronously.
     */
    SYNCHRONOUSLY_FAILED_LRO_NO_POLL,
}
