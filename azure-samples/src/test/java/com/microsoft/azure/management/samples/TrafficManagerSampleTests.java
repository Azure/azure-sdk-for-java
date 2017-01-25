/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.trafficmanager.samples.ManageSimpleTrafficManager;
import com.microsoft.azure.management.trafficmanager.samples.ManageTrafficManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TrafficManagerSampleTests extends SamplesTestBase {
    @Test
    public void testManageSimpleTrafficManager() {
        Assert.assertTrue(ManageSimpleTrafficManager.runSample(azure));
    }

    @Test
    @Ignore("Failing -  The subscription is not registered to use namespace 'Microsoft.DomainRegistration'")
    public void testManageTrafficManager() {
        Assert.assertTrue(ManageTrafficManager.runSample(azure));
    }
}
