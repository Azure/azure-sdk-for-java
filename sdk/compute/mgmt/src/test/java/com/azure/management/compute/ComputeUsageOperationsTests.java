/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComputeUsageOperationsTests extends ComputeManagementTest {
    @Test
    public void canListComputeUsages() throws Exception {
        PagedIterable<ComputeUsage> usages = computeManager.usages().listByRegion(Region.US_EAST);
        Assertions.assertTrue(TestUtilities.getSize(usages) > 0);
    }
}
