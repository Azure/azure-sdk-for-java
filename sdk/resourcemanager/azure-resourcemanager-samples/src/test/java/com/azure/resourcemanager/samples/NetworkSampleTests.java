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
import com.azure.resourcemanager.network.samples.ManagePrivateLink;
import com.azure.resourcemanager.network.samples.ManageSimpleApplicationGateway;
import com.azure.resourcemanager.network.samples.ManageVirtualMachinesInParallelWithNetwork;
import com.azure.resourcemanager.network.samples.ManageVirtualNetwork;
import com.azure.resourcemanager.network.samples.ManageVirtualNetworkAsync;
import com.azure.resourcemanager.network.samples.ManageVpnGatewayPoint2SiteConnection;
import com.azure.resourcemanager.network.samples.ManageVpnGatewaySite2SiteConnection;
import com.azure.resourcemanager.network.samples.VerifyNetworkPeeringWithNetworkWatcher;
import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public class NetworkSampleTests extends SamplesTestBase {

    @Test
    public void testManageNetworkPeeringInSameSubscription() {
        Assertions.assertTrue(ManageNetworkPeeringInSameSubscription.runSample(azureResourceManager));
    }

    @Test
    public void testVerifyNetworkPeeringWithNetworkWatcher() {
        Assertions.assertTrue(VerifyNetworkPeeringWithNetworkWatcher.runSample(azureResourceManager));
    }

    @Test
    public void testManageApplicationGateway() throws IOException {
        Assertions.assertTrue(ManageApplicationGateway.runSample(azureResourceManager));
    }

    @Test
    public void testManageInternalLoadBalancer() {
        Assertions.assertTrue(ManageInternalLoadBalancer.runSample(azureResourceManager));
    }

    @Test
    public void testCreateSimpleInternetFacingLoadBalancer() {
        Assertions.assertTrue(CreateSimpleInternetFacingLoadBalancer.runSample(azureResourceManager));
    }

    @Test
    public void testManageInternetFacingLoadBalancer() {
        Assertions.assertTrue(ManageInternetFacingLoadBalancer.runSample(azureResourceManager));
    }

    @Test
    public void testManageIPAddress() {
        Assertions.assertTrue(ManageIPAddress.runSample(azureResourceManager));
    }

    @Test
    public void testManageNetworkInterface() {
        Assertions.assertTrue(ManageNetworkInterface.runSample(azureResourceManager));
    }

    @Test
    public void testManageNetworkSecurityGroup() throws UnsupportedEncodingException, JSchException {
        Assertions.assertTrue(ManageNetworkSecurityGroup.runSample(azureResourceManager));
    }

    @Test
    public void testManageSimpleApplicationGateway() throws IOException {
        Assertions.assertTrue(ManageSimpleApplicationGateway.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachinesInParallelWithNetwork() {
        Assertions.assertTrue(ManageVirtualMachinesInParallelWithNetwork.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualNetwork() {
        Assertions.assertTrue(ManageVirtualNetwork.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualNetworkAsync() {
        Assertions.assertTrue(ManageVirtualNetworkAsync.runSample(azureResourceManager));
    }

    @Test
    public void testManageVpnGatewaySite2SiteConnection() {
        Assertions.assertTrue(ManageVpnGatewaySite2SiteConnection.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Need root certificate file and client certificate thumbprint to run the sample")
    public void testManageVpnGatewayPoint2SiteConnection() throws IOException {
        Assertions.assertTrue(ManageVpnGatewayPoint2SiteConnection.runSample(azureResourceManager));
    }

    @Test
    public void testManagePrivateLink() throws JSchException, UnsupportedEncodingException, MalformedURLException {
        Assertions.assertTrue(ManagePrivateLink.runSample(azureResourceManager));
    }
}
