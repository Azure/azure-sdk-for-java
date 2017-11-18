/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ComputeUsageOperationsTests extends ComputeManagementTest {
    @Test
    public void canListComputeUsages() throws Exception {
        List<ComputeUsage> usages = computeManager.usages().listByRegion(Region.US_EAST);
        Assert.assertTrue(usages.size() > 0);
    }
}
