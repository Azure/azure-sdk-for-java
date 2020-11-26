// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.telemetry;

/**
 * To provide subscription id.
 */
public interface SubscriptionSupplier {

    String getSubscriptionId();
}
