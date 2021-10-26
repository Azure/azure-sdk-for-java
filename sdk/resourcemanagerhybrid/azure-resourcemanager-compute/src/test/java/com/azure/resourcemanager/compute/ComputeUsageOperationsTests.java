// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.ComputeUsage;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComputeUsageOperationsTests extends ComputeManagementTest {
    @Test
    public void canListComputeUsages() throws Exception {
        PagedIterable<ComputeUsage> usages = computeManager.usages().listByRegion(Region.US_EAST);
        Assertions.assertTrue(TestUtilities.getSize(usages) > 0);
    }
}
