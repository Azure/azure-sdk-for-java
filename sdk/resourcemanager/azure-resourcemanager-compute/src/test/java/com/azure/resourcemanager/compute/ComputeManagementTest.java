// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import com.jcraft.jsch.JSch;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;

public abstract class ComputeManagementTest extends ResourceManagerTestBase {

    protected ResourceManager resourceManager;
    protected ComputeManager computeManager;
    protected NetworkManager networkManager;
    protected StorageManager storageManager;
    protected AuthorizationManager authorizationManager;
    protected KeyVaultManager keyVaultManager;
    protected MsiManager msiManager;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        computeManager = buildManager(ComputeManager.class, httpPipeline, profile);
        networkManager = buildManager(NetworkManager.class, httpPipeline, profile);
        storageManager = buildManager(StorageManager.class, httpPipeline, profile);
        keyVaultManager = buildManager(KeyVaultManager.class, httpPipeline, profile);
        authorizationManager = buildManager(AuthorizationManager.class, httpPipeline, profile);
        msiManager = buildManager(MsiManager.class, httpPipeline, profile);
        resourceManager = computeManager.resourceManager();
        setInternalContext(internalContext, computeManager, networkManager, keyVaultManager, msiManager);
    }

    @Override
    protected void cleanUpResources() {
    }

    protected void deprovisionAgentInLinuxVM(VirtualMachine virtualMachine) {
        System.out.println("Trying to de-provision");

        virtualMachine.manager().serviceClient().getVirtualMachines().beginRunCommand(
            virtualMachine.resourceGroupName(), virtualMachine.name(),
            new RunCommandInput()
                .withCommandId("RunShellScript")
                .withScript(Collections.singletonList("sudo waagent -deprovision+user --force")));

        // wait as above command will not return as sync
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));
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
            Assertions.fail("SSH connection failed" + e.getMessage());
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    protected void sleep(long milli) {
        if (isPlaybackMode()) {
            return;
        }
        try {
            Thread.sleep(milli);
        } catch (InterruptedException exception) {
        }
    }

    protected LoadBalancer createHttpLoadBalancers(Region region, ResourceGroup resourceGroup, String id)
        throws Exception {
        final String loadBalancerName = generateRandomResourceName("extlb" + id + "-", 18);
        final String publicIpName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName = loadBalancerName + "-BAP1";
        final String natPoolName = loadBalancerName + "-INP1";

        PublicIpAddress publicIPAddress =
            this
                .networkManager
                .publicIpAddresses()
                .define(publicIpName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIpName)
                .create();

        LoadBalancer loadBalancer =
            this
                .networkManager
                .loadBalancers()
                .define(loadBalancerName)
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
                .withExistingPublicIpAddress(publicIPAddress)
                .attach()
                // Add an HTTP probe
                .defineHttpProbe("httpProbe")
                .withRequestPath("/")
                .attach()
                .create();
        return loadBalancer;
    }

    protected LoadBalancer createInternetFacingLoadBalancer(
        Region region, ResourceGroup resourceGroup, String id, LoadBalancerSkuType lbSkuType) throws Exception {
        final String loadBalancerName = generateRandomResourceName("extlb" + id + "-", 18);
        final String publicIPName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";

        // Sku of PublicIP and LoadBalancer must match
        //
        PublicIPSkuType publicIPSkuType =
            lbSkuType.equals(LoadBalancerSkuType.BASIC) ? PublicIPSkuType.BASIC : PublicIPSkuType.STANDARD;

        PublicIpAddress publicIPAddress =
            this
                .networkManager
                .publicIpAddresses()
                .define(publicIPName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIPName)
                // Optionals
                .withStaticIP()
                .withSku(publicIPSkuType)
                // Create
                .create();

        LoadBalancer loadBalancer =
            this
                .networkManager
                .loadBalancers()
                .define(loadBalancerName)
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
                .withExistingPublicIpAddress(publicIPAddress) // Frontend with PIP means internet-facing load-balancer
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

    protected LoadBalancer createInternalLoadBalancer(
        Region region, ResourceGroup resourceGroup, Network network, String id) throws Exception {
        final String loadBalancerName = generateRandomResourceName("InternalLb" + id + "-", 18);
        final String privateFrontEndName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";
        final String subnetName = "subnet1";

        LoadBalancer loadBalancer =
            this
                .networkManager
                .loadBalancers()
                .define(loadBalancerName)
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
