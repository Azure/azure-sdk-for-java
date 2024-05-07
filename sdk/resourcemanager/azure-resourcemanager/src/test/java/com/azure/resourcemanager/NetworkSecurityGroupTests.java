// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class NetworkSecurityGroupTests extends ResourceManagerTestProxyTestBase {

    protected AzureResourceManager azureResourceManager;

    protected String rgName = "";
    protected final Region region = Region.US_EAST;

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
        azureResourceManager = buildManager(AzureResourceManager.class, httpPipeline, profile);
        setInternalContext(internalContext, azureResourceManager);

        rgName = generateRandomResourceName("javacsmrg", 15);
    }

    @Override
    protected void cleanUpResources() {
        try {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
        }
    }


    @Test
    public void testDeleteByResourceGroup() {
        Network network = azureResourceManager.networks().define("vmssvnet")
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        final String nsgName = generateRandomResourceName("nsg", 8);

        NetworkSecurityGroup nsg = azureResourceManager.networkSecurityGroups().define(nsgName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .defineRule("rule1")
            .allowOutbound()
            .fromAnyAddress()
            .fromAnyPort()
            .toAnyAddress()
            .toPort(80)
            .withProtocol(SecurityRuleProtocol.TCP)
            .attach()
            .create();

        final String vmssName = generateRandomResourceName("vmss", 10);

        VirtualMachineScaleSet vmss = azureResourceManager.virtualMachineScaleSets().define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withFlexibleOrchestrationMode()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withVirtualMachinePublicIp()
            // NetworkSecurityGroup
            .withExistingNetworkSecurityGroup(nsg)
            .create();

        // delete NetworkSecurityGroup (with NIC references)
        /*
         * "Mono.defer" is added to put the "deleteByResourceGroupAsync" call to Schedulers.parallel().
         * So that any blocking call there would be reported by Reactor during the test.
         */
        Mono.defer(() -> azureResourceManager.networkSecurityGroups().deleteByResourceGroupAsync(rgName, nsgName))
            .subscribeOn(Schedulers.parallel())
            .block();
    }
}
