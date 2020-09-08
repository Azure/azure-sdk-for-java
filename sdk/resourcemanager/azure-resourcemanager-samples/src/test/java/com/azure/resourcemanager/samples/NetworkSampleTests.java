// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.network.samples.CreateSimpleInternetFacingLoadBalancer;
import com.azure.resourcemanager.network.samples.ManageApplicationGateway;
import com.azure.resourcemanager.network.samples.ManageIPAddress;
import com.azure.resourcemanager.network.samples.ManageInternalLoadBalancer;
import com.azure.resourcemanager.network.samples.ManageInternetFacingLoadBalancer;
import com.azure.resourcemanager.network.samples.ManageNetworkInterface;
import com.azure.resourcemanager.network.samples.ManageNetworkPeeringInSameSubscription;
import com.azure.resourcemanager.network.samples.ManageNetworkSecurityGroup;
import com.azure.resourcemanager.network.samples.ManageSimpleApplicationGateway;
import com.azure.resourcemanager.network.samples.ManageVirtualMachinesInParallelWithNetwork;
import com.azure.resourcemanager.network.samples.ManageVirtualNetwork;
import com.azure.resourcemanager.network.samples.ManageVirtualNetworkAsync;
import com.azure.resourcemanager.network.samples.ManageVpnGatewayPoint2SiteConnection;
import com.azure.resourcemanager.network.samples.ManageVpnGatewaySite2SiteConnection;
import com.azure.resourcemanager.network.samples.VerifyNetworkPeeringWithNetworkWatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NetworkSampleTests extends SamplesTestBase {

    @Test
    public void testManageNetworkPeeringInSameSubscription() throws Exception {
        Assertions.assertTrue(ManageNetworkPeeringInSameSubscription.runSample(azure));
    }

    @Test
    @Disabled("Get error `Cannot create more than 1 network watchers for this subscription in this region.` with test subscription")
    public void testVerifyNetworkPeeringWithNetworkWatcher() throws Exception {
        Assertions.assertTrue(VerifyNetworkPeeringWithNetworkWatcher.runSample(azure));
    }

    @Test
    public void testManageApplicationGateway() throws Exception {
        Assertions.assertTrue(ManageApplicationGateway.runSample(azure));
    }

    @Test
    public void testManageInternalLoadBalancer() throws Exception {
        Assertions.assertTrue(ManageInternalLoadBalancer.runSample(azure));
    }

    @Test
    public void testCreateSimpleInternetFacingLoadBalancer() throws Exception {
        Assertions.assertTrue(CreateSimpleInternetFacingLoadBalancer.runSample(azure));
    }

    @Test
    public void testManageInternetFacingLoadBalancer() throws Exception {
        Assertions.assertTrue(ManageInternetFacingLoadBalancer.runSample(azure));
    }

    @Test
    public void testManageIPAddress() throws Exception {
        Assertions.assertTrue(ManageIPAddress.runSample(azure));
    }

    @Test
    public void testManageNetworkInterface() throws Exception {
        Assertions.assertTrue(ManageNetworkInterface.runSample(azure));
    }

    @Test
    public void testManageNetworkSecurityGroup() throws Exception {
        Assertions.assertTrue(ManageNetworkSecurityGroup.runSample(azure));
    }

    @Test
    public void testManageSimpleApplicationGateway() throws Exception {
        Assertions.assertTrue(ManageSimpleApplicationGateway.runSample(azure));
    }

    @Test
    public void testManageVirtualMachinesInParallelWithNetwork() throws Exception {
        Assertions.assertTrue(ManageVirtualMachinesInParallelWithNetwork.runSample(azure));
    }

    @Test
    public void testManageVirtualNetwork() throws Exception {
        Assertions.assertTrue(ManageVirtualNetwork.runSample(azure));
    }

    @Test
    public void testManageVirtualNetworkAsync() throws Exception {
        Assertions.assertTrue(ManageVirtualNetworkAsync.runSample(azure));
    }

    @Test
    public void testManageVpnGatewaySite2SiteConnection() throws Exception {
        Assertions.assertTrue(ManageVpnGatewaySite2SiteConnection.runSample(azure));
    }

    @Test
    @Disabled("Need root certificate file and client certificate thumbprint to run the sample")
    public void testManageVpnGatewayPoint2SiteConnection() throws Exception {
        Assertions.assertTrue(ManageVpnGatewayPoint2SiteConnection.runSample(azure));
    }
}
