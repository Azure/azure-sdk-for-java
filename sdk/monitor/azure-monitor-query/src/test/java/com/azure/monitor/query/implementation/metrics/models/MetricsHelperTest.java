// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.metrics.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricsHelperTest {
    private static final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    public void extractsSubscriptionFromResourceId() {
        String resourceId = "/subscriptions/" + SUBSCRIPTION_ID
            + "/resourceGroups/test/providers/Microsoft.Compute/virtualMachines/test";

        assertEquals(SUBSCRIPTION_ID, MetricsHelper.getSubscriptionFromResourceId(resourceId));
    }

    @Test
    public void extractsSubscriptionFromSubscriptionResourceId() {
        String resourceId = "/subscriptions/" + SUBSCRIPTION_ID;

        assertEquals(SUBSCRIPTION_ID, MetricsHelper.getSubscriptionFromResourceId(resourceId));
    }
}
