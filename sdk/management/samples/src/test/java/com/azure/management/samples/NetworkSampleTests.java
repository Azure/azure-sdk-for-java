/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.network.samples.CreateSimpleInternetFacingLoadBalancer;
import com.azure.management.network.samples.ManageApplicationGateway;
import com.azure.management.network.samples.ManageIPAddress;
import com.azure.management.network.samples.ManageInternalLoadBalancer;
import com.azure.management.network.samples.ManageInternetFacingLoadBalancer;
import com.azure.management.network.samples.ManageNetworkInterface;
import com.azure.management.network.samples.ManageNetworkPeeringInSameSubscription;
import com.azure.management.network.samples.ManageNetworkSecurityGroup;
import com.azure.management.network.samples.ManageSimpleApplicationGateway;
import com.azure.management.network.samples.ManageVirtualMachinesInParallelWithNetwork;
import com.azure.management.network.samples.ManageVirtualNetwork;
import com.azure.management.network.samples.ManageVirtualNetworkAsync;
import com.azure.management.network.samples.ManageVpnGatewayPoint2SiteConnection;
import com.azure.management.network.samples.ManageVpnGatewaySite2SiteConnection;
import com.azure.management.network.samples.VerifyNetworkPeeringWithNetworkWatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NetworkSampleTests extends SamplesTestBase {

    @Test
    public void testManageNetworkPeeringInSameSubscription() {
        Assertions.assertTrue(ManageNetworkPeeringInSameSubscription.runSample(azure));
    }

    @Test
    @Disabled("Get error `Cannot create more than 1 network watchers for this subscription in this region.` with test subscription")
    public void testVerifyNetworkPeeringWithNetworkWatcher() {
        Assertions.assertTrue(VerifyNetworkPeeringWithNetworkWatcher.runSample(azure));
    }

    @Test
    public void testManageApplicationGateway() {
        Assertions.assertTrue(ManageApplicationGateway.runSample(azure));
    }

    @Test
    public void testManageInternalLoadBalancer() {
        Assertions.assertTrue(ManageInternalLoadBalancer.runSample(azure));
    }

    @Test
    public void testCreateSimpleInternetFacingLoadBalancer() {
        Assertions.assertTrue(CreateSimpleInternetFacingLoadBalancer.runSample(azure));
    }

    @Test
    public void testManageInternetFacingLoadBalancer() {
        Assertions.assertTrue(ManageInternetFacingLoadBalancer.runSample(azure));
    }

    @Test
    public void testManageIPAddress() {
        Assertions.assertTrue(ManageIPAddress.runSample(azure));
    }

    @Test
    public void testManageNetworkInterface() {
        Assertions.assertTrue(ManageNetworkInterface.runSample(azure));
    }

    @Test
    public void testManageNetworkSecurityGroup() {
        Assertions.assertTrue(ManageNetworkSecurityGroup.runSample(azure));
    }

    @Test
    public void testManageSimpleApplicationGateway() {
        Assertions.assertTrue(ManageSimpleApplicationGateway.runSample(azure));
    }

    @Test
    public void testManageVirtualMachinesInParallelWithNetwork() {
        Assertions.assertTrue(ManageVirtualMachinesInParallelWithNetwork.runSample(azure));
    }

    @Test
    public void testManageVirtualNetwork() {
        Assertions.assertTrue(ManageVirtualNetwork.runSample(azure));
    }

    @Test
    public void testManageVirtualNetworkAsync() {
        Assertions.assertTrue(ManageVirtualNetworkAsync.runSample(azure));
    }

    @Test
    public void testManageVpnGatewaySite2SiteConnection() {
        Assertions.assertTrue(ManageVpnGatewaySite2SiteConnection.runSample(azure));
    }

    @Test
    @Disabled("Need root certificate file and client certificate thumbprint to run the sample")
    public void testManageVpnGatewayPoint2SiteConnection() {
        Assertions.assertTrue(ManageVpnGatewayPoint2SiteConnection.runSample(azure));
    }
}
