// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.models.NetworkUsage;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkUsageOperationsTests extends NetworkManagementTest {
    @Test
    public void canListNetworkUsages() throws Exception {
        PagedIterable<NetworkUsage> usages = networkManager.usages().listByRegion(Region.US_EAST);
        Assertions.assertTrue(TestUtilities.getSize(usages) > 0);
    }

    @Override
    protected void cleanUpResources() {
    }
}
