/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.network.samples.*;
import org.junit.Assert;
import org.junit.Test;

public class NetworkSampleTests extends SamplesTestBase {

    @Test
    public void testManageApplicationGateway() {
        Assert.assertTrue(ManageApplicationGateway.runSample(azure));
    }

    @Test
    public void testManageInternalLoadBalancer() {
        Assert.assertTrue(ManageInternalLoadBalancer.runSample(azure));
    }

    @Test
    public void testManageInternetFacingLoadBalancer() {
        Assert.assertTrue(ManageInternetFacingLoadBalancer.runSample(azure));
    }

    @Test
    public void testManageIPAddress() {
        Assert.assertTrue(ManageIPAddress.runSample(azure));
    }

    @Test
    public void testManageNetworkInterface() {
        Assert.assertTrue(ManageNetworkInterface.runSample(azure));
    }

    @Test
    public void testManageNetworkSecurityGroup() {
        Assert.assertTrue(ManageNetworkSecurityGroup.runSample(azure));
    }

    @Test
    public void testManageSimpleApplicationGateway() {
        Assert.assertTrue(ManageSimpleApplicationGateway.runSample(azure));
    }

    @Test
    public void testManageVirtualMachinesInParallelWithNetwork() {
        Assert.assertTrue(ManageVirtualMachinesInParallelWithNetwork.runSample(azure));
    }

    @Test
    public void testManageVirtualNetwork() {
        Assert.assertTrue(ManageVirtualNetwork.runSample(azure));
    }
}
