/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerSku;
import com.microsoft.azure.management.network.LoadBalancerSkuType;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPSkuType;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ComputeManagementTest extends TestBase {
    public ComputeManagementTest() {
        super(TestBase.RunCondition.BOTH);
    }

    public ComputeManagementTest(TestBase.RunCondition runCondition) {
        super(runCondition);
    }

    protected ResourceManager resourceManager;
    protected ComputeManager computeManager;
    protected NetworkManager networkManager;
    protected StorageManager storageManager;
    protected GraphRbacManager rbacManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        computeManager = ComputeManager
                .authenticate(restClient, defaultSubscription);

        networkManager = NetworkManager
                .authenticate(restClient, defaultSubscription);

        storageManager = StorageManager
                .authenticate(restClient, defaultSubscription);

        rbacManager = GraphRbacManager.authenticate(restClient, domain);
    }

    @Override
    protected void cleanUpResources() {
    }

    protected void deprovisionAgentInLinuxVM(String host, int port, String userName, String password) {
        if (isPlaybackMode()) {
            return;
        }
        SshShell shell = null;
        try {
            System.out.println("Trying to de-provision");
            shell = SshShell.open(host, port, userName, password);
            List<String> deprovisionCommand = new ArrayList<>();
            deprovisionCommand.add("sudo waagent -deprovision+user --force");
            String output = shell.runCommands(deprovisionCommand);
            System.out.println(output);
        } catch (JSchException jSchException) {
            Assert.assertNull(jSchException.getMessage(), jSchException);
        } catch (IOException ioException) {
            Assert.assertNull(ioException.getMessage(), ioException);
        } catch (Exception exception) {
            Assert.assertNull(exception.getMessage(), exception);
        } finally {
            if (shell != null) {
                shell.close();
            }
        }
    }

    protected void ensureCanDoSsh(String fqdn, int sshPort, String uname, String password) {
        if (isPlaybackMode()) {
            return;
        }
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession(uname, fqdn, sshPort);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
        } catch (Exception e) {
            Assert.fail("SSH connection failed" + e.getMessage());
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    protected  void sleep(long milli) {
        if (isPlaybackMode()) {
            return;
        }
        try {
            Thread.sleep(milli);
        } catch (InterruptedException exception) {
        }
    }


    protected LoadBalancer createHttpLoadBalancers(Region region, ResourceGroup resourceGroup,
                                                 String id) throws Exception {
        final String loadBalancerName = generateRandomResourceName("extlb" + id + "-", 18);
        final String publicIpName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName = loadBalancerName + "-BAP1";
        final String natPoolName = loadBalancerName + "-INP1";

        PublicIPAddress publicIPAddress = this.networkManager.publicIPAddresses().define(publicIpName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIpName)
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers().define(loadBalancerName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(80)
                    .toBackend(backendPoolName)
                    .withProbe("httpProbe")
                    .attach()
                .defineInboundNatPool(natPoolName)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPortRange(5000, 5099)
                    .toBackendPort(22)
                    .attach()
                // Explicitly define the frontend
                .definePublicFrontend(frontendName)
                    .withExistingPublicIPAddress(publicIPAddress)
                    .attach()
                // Add an HTTP probe
                .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()

                .create();
        return loadBalancer;

    }

    protected LoadBalancer createInternetFacingLoadBalancer(Region region, ResourceGroup resourceGroup, String id, LoadBalancerSkuType lbSkuType) throws Exception {
        final String loadBalancerName = generateRandomResourceName("extlb" + id + "-", 18);
        final String publicIPName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";

        // Sku of PublicIP and LoadBalancer must match
        //
        PublicIPSkuType publicIPSkuType = lbSkuType.equals(LoadBalancerSkuType.BASIC) ? PublicIPSkuType.BASIC : PublicIPSkuType.STANDARD;

        PublicIPAddress publicIPAddress = this.networkManager.publicIPAddresses().define(publicIPName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIPName)
                // Optionals
                .withStaticIP()
                .withSku(publicIPSkuType)
                // Create
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers().define(loadBalancerName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)

                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(80)
                    .toBackend(backendPoolName1)
                    .withProbe("httpProbe")
                    .attach()
                .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(443)
                    .toBackend(backendPoolName2)
                    .withProbe("httpsProbe")
                    .attach()

                // Add two nat pools to enable direct VM connectivity to port SSH and 23
                .defineInboundNatPool(natPoolName1)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPortRange(5000, 5099)
                    .toBackendPort(22)
                    .attach()
                .defineInboundNatPool(natPoolName2)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPortRange(6000, 6099)
                    .toBackendPort(23)
                    .attach()

                // Explicitly define the frontend
                .definePublicFrontend(frontendName)
                    .withExistingPublicIPAddress(publicIPAddress)   // Frontend with PIP means internet-facing load-balancer
                    .attach()

                    // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()
                .defineHttpProbe("httpsProbe")
                    .withRequestPath("/")
                    .attach()
                .withSku(lbSkuType)
                .create();
        return loadBalancer;
    }

    protected LoadBalancer createInternalLoadBalancer(Region region, ResourceGroup resourceGroup,
                                                    Network network, String id) throws Exception {
        final String loadBalancerName = generateRandomResourceName("InternalLb" + id + "-", 18);
        final String privateFrontEndName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";
        final String subnetName = "subnet1";

        LoadBalancer loadBalancer = this.networkManager.loadBalancers().define(loadBalancerName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(1000)
                    .toBackend(backendPoolName1)
                    .withProbe("httpProbe")
                    .attach()
                .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(1001)
                    .toBackend(backendPoolName2)
                    .withProbe("httpsProbe")
                    .attach()

                // Add two NAT pools to enable direct VM connectivity to port 44 and 45
                .defineInboundNatPool(natPoolName1)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPortRange(8000, 8099)
                    .toBackendPort(44)
                    .attach()
                .defineInboundNatPool(natPoolName2)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPortRange(9000, 9099)
                    .toBackendPort(45)
                    .attach()

                // Explicitly define the frontend
                .definePrivateFrontend(privateFrontEndName)
                    .withExistingSubnet(network, subnetName) // Frontend with VNET means internal load-balancer
                    .attach()

                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()
                .defineHttpProbe("httpsProbe")
                    .withRequestPath("/")
                    .attach()

                .create();
        return loadBalancer;
    }
}
