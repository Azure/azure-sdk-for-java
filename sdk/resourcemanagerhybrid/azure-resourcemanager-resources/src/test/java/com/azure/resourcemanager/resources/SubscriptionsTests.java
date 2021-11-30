// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubscriptionsTests extends ResourceManagementTest {
    @Test
    public void canListSubscriptions() {
        PagedIterable<Subscription> subscriptions = resourceClient.subscriptions().list();
        Assertions.assertTrue(TestUtilities.getSize(subscriptions) > 0);
    }

    @Test
    public void canListLocations() {
        PagedIterable<Location> locations = resourceClient.subscriptions().list().iterator().next().listLocations();
        Assertions.assertTrue(TestUtilities.getSize(locations) > 0);
    }
}
