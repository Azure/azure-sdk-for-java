package com.microsoft.azure.management.compute;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
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
    protected ResourceManager resourceManager;
    protected ComputeManager computeManager;
    protected NetworkManager networkManager;
    protected StorageManager storageManager;

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
    }

    @Override
    protected void cleanUpResources() {
    }

    protected void deprovisionAgentInLinuxVM(String host, int port, String userName, String password) {
        if (IS_MOCKED) {
            return;
        }
        SSHShell shell = null;
        try {
            System.out.println("Trying to de-provision");
            shell = SSHShell.open(host, port, userName, password);
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
        if (IS_MOCKED) {
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
        if (IS_MOCKED) {
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

        PublicIpAddress publicIpAddress = this.networkManager.publicIpAddresses().define(publicIpName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIpName)
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers().define(loadBalancerName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .definePublicFrontend(frontendName)
                    .withExistingPublicIpAddress(publicIpAddress)
                    .attach()
                .defineBackend(backendPoolName)
                    .attach()
                .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()

                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPort(80)
                    .withProbe("httpProbe")
                    .withBackend(backendPoolName)
                    .attach()
                .defineInboundNatPool(natPoolName)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPortRange(5000, 5099)
                    .withBackendPort(22)
                    .attach()
                .create();
        return loadBalancer;

    }

    protected LoadBalancer createInternetFacingLoadBalancer(Region region, ResourceGroup resourceGroup, String id) throws Exception {
        final String loadBalancerName = generateRandomResourceName("extlb" + id + "-", 18);
        final String publicIpName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";

        PublicIpAddress publicIpAddress = this.networkManager.publicIpAddresses().define(publicIpName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIpName)
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers().define(loadBalancerName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .definePublicFrontend(frontendName)
                    .withExistingPublicIpAddress(publicIpAddress)
                    .attach()

                // Add two backend one per rule
                .defineBackend(backendPoolName1)
                    .attach()
                .defineBackend(backendPoolName2)
                    .attach()

                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()
                .defineHttpProbe("httpsProbe")
                    .withRequestPath("/")
                    .attach()

                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPort(80)
                    .withProbe("httpProbe")
                    .withBackend(backendPoolName1)
                    .attach()
                .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPort(443)
                    .withProbe("httpsProbe")
                    .withBackend(backendPoolName2)
                    .attach()

                // Add two nat pools to enable direct VM connectivity to port SSH and 23
                .defineInboundNatPool(natPoolName1)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPortRange(5000, 5099)
                    .withBackendPort(22)
                    .attach()
                .defineInboundNatPool(natPoolName2)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPortRange(6000, 6099)
                    .withBackendPort(23)
                    .attach()
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
                .definePrivateFrontend(privateFrontEndName)
                    .withExistingSubnet(network, subnetName)
                    .attach()

                // Add two backend one per rule
                .defineBackend(backendPoolName1)
                    .attach()
                .defineBackend(backendPoolName2)
                    .attach()

                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()
                .defineHttpProbe("httpsProbe")
                    .withRequestPath("/")
                    .attach()

                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(privateFrontEndName)
                    .withFrontendPort(1000)
                    .withProbe("httpProbe")
                    .withBackend(backendPoolName1)
                    .attach()
                .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(privateFrontEndName)
                    .withFrontendPort(1001)
                    .withProbe("httpsProbe")
                    .withBackend(backendPoolName2)
                    .attach()

                // Add two nat pools to enable direct VM connectivity to port 44 and 45
                .defineInboundNatPool(natPoolName1)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(privateFrontEndName)
                    .withFrontendPortRange(8000, 8099)
                    .withBackendPort(44)
                    .attach()
                .defineInboundNatPool(natPoolName2)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(privateFrontEndName)
                    .withFrontendPortRange(9000, 9099)
                    .withBackendPort(45)
                    .attach()
                .create();
        return loadBalancer;
    }
}
