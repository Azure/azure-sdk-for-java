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
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.LoadBalancerSku;
import com.azure.resourcemanager.containerservice.models.NetworkPlugin;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class KubernetesCniTests extends ResourceManagerTestBase {

    private AzureResourceManager azureResourceManager;
    private String rgName;

    private final Region region = Region.US_EAST;

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

        rgName = generateRandomResourceName("rg", 8);
    }

    @Override
    protected void cleanUpResources() {
        try {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
        }
    }

    @Test
    public void testKubernetesClusterCni() {
        final String vnetName = rgName + "vnet";
        final String subnetName = "default";
        final String aksName = generateRandomResourceName("aks", 15);
        final String dnsPrefix = generateRandomResourceName("dns", 10);
        final String agentPoolName = generateRandomResourceName("ap0", 10);
        final String roleAssignmentName = generateRandomUuid();

        Network vnet = azureResourceManager.networks().define(vnetName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/8")
            .withSubnet(subnetName, "10.240.0.0/16")
            .create();

        KubernetesCluster kubernetesCluster = azureResourceManager.kubernetesClusters().define(aksName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(sshPublicKey())
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(3)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withMaxPodsCount(30)
                .withAvailabilityZones(1, 2, 3)
                .withVirtualNetwork(vnet.id(), subnetName)
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .defineNetworkProfile()
                .withNetworkPlugin(NetworkPlugin.AZURE)
                .withServiceCidr("10.0.0.0/16")
                .withDnsServiceIP("10.0.0.10")
                .withDockerBridgeCidr("172.17.0.1/16")
                .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                .attach()
            .create();

        azureResourceManager.accessManagement().roleAssignments().define(roleAssignmentName)
            .forObjectId(kubernetesCluster.systemAssignedManagedServiceIdentityPrincipalId())
            .withBuiltInRole(BuiltInRole.NETWORK_CONTRIBUTOR)
            .withScope(vnet.subnets().get(subnetName).id())
            .create();
    }
}
