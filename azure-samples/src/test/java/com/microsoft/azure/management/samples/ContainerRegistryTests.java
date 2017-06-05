/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;
import com.microsoft.azure.management.containerregistry.samples.ManageContainerRegistry;
import com.microsoft.azure.management.resources.core.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class ContainerRegistryTests extends SamplesTestBase {
    public ContainerRegistryTests() {
        // Failing in playback - dependent on Docker client/glassfish jersey library which is expecting a real connection
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageContainerRegistry() {
        Assert.assertTrue(ManageContainerRegistry.runSample(azure));
    }
}
