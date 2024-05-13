// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.resourcemanager.containerservice.models.AgentPool;
import com.azure.resourcemanager.containerservice.models.AgentPoolData;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.Code;
import com.azure.resourcemanager.containerservice.models.ContainerServiceResourceTypes;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.CredentialResult;
import com.azure.resourcemanager.containerservice.models.Format;
import com.azure.resourcemanager.containerservice.models.KubeletDiskType;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.containerservice.models.KubernetesSupportPlan;
import com.azure.core.management.Region;
import com.azure.resourcemanager.containerservice.models.ManagedClusterPropertiesAutoScalerProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterSkuTier;
import com.azure.resourcemanager.containerservice.models.NetworkDataplane;
import com.azure.resourcemanager.containerservice.models.NetworkMode;
import com.azure.resourcemanager.containerservice.models.NetworkPlugin;
import com.azure.resourcemanager.containerservice.models.NetworkPluginMode;
import com.azure.resourcemanager.containerservice.models.NetworkPolicy;
import com.azure.resourcemanager.containerservice.models.OSDiskType;
import com.azure.resourcemanager.containerservice.models.OrchestratorVersionProfile;
import com.azure.resourcemanager.containerservice.models.PublicNetworkAccess;
import com.azure.resourcemanager.containerservice.models.ScaleSetEvictionPolicy;
import com.azure.resourcemanager.containerservice.models.ScaleSetPriority;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.rest.ActivationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KubernetesClustersTests extends ContainerServiceManagementTest {
    private static final ClientLogger LOGGER = new ClientLogger(KubernetesClustersTests.class);

    private static final String SSH_KEY = sshPublicKey();

    @Test
    public void canCRUDKubernetesCluster() {
        // enable preview feature of ACR Teleport for AKS
        Context context = new Context(
            AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY,
            new HttpHeaders().set("EnableACRTeleport", "true"));

        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);
        String agentPoolName2 = generateRandomResourceName("ap2", 10);

        String agentPoolResourceGroupName = generateRandomResourceName("pool", 15);

        /*
        KubeletDiskType requires registering following preview feature:
            azure.features().register("Microsoft.ContainerService", "KubeletDisk");
         */

        // create
        KubernetesCluster kubernetesCluster =
            containerServiceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(Region.US_WEST2)
                .withExistingResourceGroup(rgName)
                .withDefaultVersion()
                .withRootUsername("testaks")
                .withSshKey(SSH_KEY)
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool(agentPoolName)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_F4S_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .withOSDiskSizeInGB(30)
                    .withOSDiskType(OSDiskType.EPHEMERAL)
                    .withKubeletDiskType(KubeletDiskType.TEMPORARY)
                    .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .withTag("pool.name", agentPoolName)
                    .attach()
                .defineAgentPool(agentPoolName1)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .withTag("pool.name", agentPoolName1)
                    .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .withTag("tag1", "value1")
                .withAgentPoolResourceGroup(agentPoolResourceGroupName)
                .create(context);

        Assertions.assertNotNull(kubernetesCluster.id());
        Assertions.assertEquals(Region.US_WEST2, kubernetesCluster.region());
        Assertions.assertEquals("testaks", kubernetesCluster.linuxRootUsername());
        Assertions.assertEquals(2, kubernetesCluster.agentPools().size());
        Assertions.assertEquals(agentPoolResourceGroupName, kubernetesCluster.agentPoolResourceGroup());

        Assertions.assertEquals(agentPoolResourceGroupName, resourceManager.resourceGroups().getByName(agentPoolResourceGroupName).name());

        KubernetesClusterAgentPool agentPool = kubernetesCluster.agentPools().get(agentPoolName);
        Assertions.assertNotNull(agentPool);
        Assertions.assertEquals(1, agentPool.count());
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_F4S_V2, agentPool.vmSize());
        Assertions.assertEquals(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS, agentPool.type());
        Assertions.assertEquals(AgentPoolMode.SYSTEM, agentPool.mode());
        Assertions.assertEquals(OSDiskType.EPHEMERAL, agentPool.osDiskType());
        Assertions.assertEquals(30, agentPool.osDiskSizeInGB());
        Assertions.assertEquals(KubeletDiskType.TEMPORARY, agentPool.kubeletDiskType());
        Assertions.assertEquals(Collections.singletonMap("pool.name", agentPoolName), agentPool.tags());

        agentPool = kubernetesCluster.agentPools().get(agentPoolName1);
        Assertions.assertNotNull(agentPool);
        Assertions.assertEquals(1, agentPool.count());
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_A2_V2, agentPool.vmSize());
        Assertions.assertEquals(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS, agentPool.type());
        Assertions.assertEquals(Collections.singletonMap("pool.name", agentPoolName1), agentPool.tags());

        Assertions.assertNotNull(kubernetesCluster.tags().get("tag1"));

        // stop
        kubernetesCluster.stop();
        kubernetesCluster.refresh();
        Assertions.assertEquals(Code.STOPPED, kubernetesCluster.powerState().code());

        // start
        kubernetesCluster.start();
        kubernetesCluster.refresh();
        Assertions.assertEquals(Code.RUNNING, kubernetesCluster.powerState().code());

        Map<String, String> nodeLables = new HashMap<>(2);
        nodeLables.put("environment", "dev");
        nodeLables.put("app.1", "spring");

        List<String> nodeTaints = new ArrayList<>(1);
        nodeTaints.add("key=value:NoSchedule");

        // update
        kubernetesCluster =
            kubernetesCluster
                .update()
                .updateAgentPool(agentPoolName1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .withAgentPoolVirtualMachineCount(2)
                    .withKubeletDiskType(KubeletDiskType.OS)
                    .withoutTag("pool.name")
                    .withTags(Collections.singletonMap("state", "updated"))
                    .parent()
                .defineAgentPool(agentPoolName2)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_F4S_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .withOSDiskSizeInGB(30)
                    .withAgentPoolMode(AgentPoolMode.USER)
                    .withOSDiskType(OSDiskType.MANAGED)
                    .withKubeletDiskType(KubeletDiskType.TEMPORARY)
                    .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                    .withNodeLabels(Collections.unmodifiableMap(nodeLables))
                    .withNodeTaints(Collections.unmodifiableList(nodeTaints))
                    .withTags(Collections.singletonMap("state", "created"))
                    .attach()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply(context);

        Assertions.assertEquals(3, kubernetesCluster.agentPools().size());

        agentPool = kubernetesCluster.agentPools().get(agentPoolName1);
        Assertions.assertEquals(2, agentPool.count());
        Assertions.assertEquals(AgentPoolMode.SYSTEM, agentPool.mode());
        Assertions.assertEquals(KubeletDiskType.OS, agentPool.kubeletDiskType());
        Assertions.assertEquals(Collections.singletonMap("state", "updated"), agentPool.tags());

        agentPool = kubernetesCluster.agentPools().get(agentPoolName2);
        Assertions.assertNotNull(agentPool);
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_F4S_V2, agentPool.vmSize());
        Assertions.assertEquals(1, agentPool.count());
        Assertions.assertEquals(OSDiskType.MANAGED, agentPool.osDiskType());
        Assertions.assertEquals(30, agentPool.osDiskSizeInGB());
        Assertions.assertEquals(KubeletDiskType.TEMPORARY, agentPool.kubeletDiskType());
        Assertions.assertEquals(Collections.singletonMap("state", "created"), agentPool.tags());
        Assertions.assertEquals(Collections.unmodifiableMap(nodeLables), agentPool.nodeLabels());
        Assertions.assertEquals("key=value:NoSchedule", agentPool.nodeTaints().iterator().next());

        Assertions.assertEquals("value2", kubernetesCluster.tags().get("tag2"));
        Assertions.assertFalse(kubernetesCluster.tags().containsKey("tag1"));

        // preview feature
//        // stop agent pool
//        agentPool.stop();
//        Assertions.assertEquals(Code.STOPPED, agentPool.powerState().code());
//
//        // start agent pool
//        agentPool.start();
//        Assertions.assertEquals(Code.RUNNING, agentPool.powerState().code());
    }

    @Test
    public void canAutoScaleKubernetesCluster() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);
        String agentPoolName2 = generateRandomResourceName("ap2", 10);
        String agentPoolName3 = generateRandomResourceName("ap2", 10);

        Map<String, String> nodeLables = new HashMap<>(2);
        nodeLables.put("environment", "dev");
        nodeLables.put("app.1", "spring");

        List<String> nodeTaints = new ArrayList<>(1);
        nodeTaints.add("key=value:NoSchedule");

        // create cluster
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_SOUTH_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withSystemAssignedManagedServiceIdentity()
            // zone redundancy
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(3)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withAvailabilityZones(1, 2, 3)
                .attach()
            // auto-scaling
            // labels and taints
            .defineAgentPool(agentPoolName1)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAutoScaling(1, 3)
                .withNodeLabels(Collections.unmodifiableMap(nodeLables))
                .withNodeTaints(Collections.unmodifiableList(nodeTaints))
                .attach()
            // number of nodes = 0
            .defineAgentPool(agentPoolName2)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                .withAgentPoolVirtualMachineCount(0)
                .withMaxPodsCount(10)
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .withAutoScalerProfile(new ManagedClusterPropertiesAutoScalerProfile().withScanInterval("30s"))
            .create();

        // print config
        LOGGER.log(LogLevel.VERBOSE,
            () -> new String(kubernetesCluster.adminKubeConfigContent(), StandardCharsets.UTF_8));

        Assertions.assertEquals(Code.RUNNING, kubernetesCluster.powerState().code());

        KubernetesClusterAgentPool agentPoolProfile = kubernetesCluster.agentPools().get(agentPoolName);
        Assertions.assertEquals(3, agentPoolProfile.nodeSize());
        Assertions.assertFalse(agentPoolProfile.isAutoScalingEnabled());
        Assertions.assertEquals(Arrays.asList("1", "2", "3"), agentPoolProfile.availabilityZones());
        Assertions.assertEquals(Code.RUNNING, agentPoolProfile.powerState().code());

        KubernetesClusterAgentPool agentPoolProfile1 = kubernetesCluster.agentPools().get(agentPoolName1);
        Assertions.assertEquals(1, agentPoolProfile1.nodeSize());
        Assertions.assertTrue(agentPoolProfile1.isAutoScalingEnabled());
        Assertions.assertEquals(1, agentPoolProfile1.minimumNodeSize());
        Assertions.assertEquals(3, agentPoolProfile1.maximumNodeSize());
        Assertions.assertEquals(Collections.unmodifiableMap(nodeLables), agentPoolProfile1.nodeLabels());
        Assertions.assertEquals("key=value:NoSchedule", agentPoolProfile1.nodeTaints().iterator().next());

        KubernetesClusterAgentPool agentPoolProfile2 = kubernetesCluster.agentPools().get(agentPoolName2);
        Assertions.assertEquals(0, agentPoolProfile2.nodeSize());
        Assertions.assertEquals(10, agentPoolProfile2.maximumPodsPerNode());

        // disable auto-scaling
        kubernetesCluster.update()
            .updateAgentPool(agentPoolName1)
                .withoutAutoScaling()
                .parent()
            .apply();

        Assertions.assertFalse(agentPoolProfile1.isAutoScalingEnabled());

        // remove agent pool
        kubernetesCluster.update()
            .withoutAgentPool(agentPoolName1)
            .withoutAgentPool(agentPoolName2)
            .defineAgentPool(agentPoolName3)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                .withAgentPoolVirtualMachineCount(0)
                .attach()
            .apply();

        Assertions.assertEquals(2, kubernetesCluster.agentPools().size());

        KubernetesClusterAgentPool agentPoolProfile3 = kubernetesCluster.agentPools().get(agentPoolName3);
        Assertions.assertEquals(0, agentPoolProfile3.nodeSize());
    }

    @Test
    public void canCreateClusterWithSpotVM() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);
        String agentPoolName2 = generateRandomResourceName("ap2", 10);

        // create cluster
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_SOUTH_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
            // spot vm
            .defineAgentPool(agentPoolName1)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withSpotPriorityVirtualMachine()
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .create();

        // print config
        LOGGER.log(LogLevel.VERBOSE,
            () -> new String(kubernetesCluster.adminKubeConfigContent(), StandardCharsets.UTF_8));

        KubernetesClusterAgentPool agentPoolProfile = kubernetesCluster.agentPools().get(agentPoolName);
        Assertions.assertTrue(agentPoolProfile.virtualMachinePriority() == null || agentPoolProfile.virtualMachinePriority() == ScaleSetPriority.REGULAR);

        KubernetesClusterAgentPool agentPoolProfile1 = kubernetesCluster.agentPools().get(agentPoolName1);
        Assertions.assertEquals(ScaleSetPriority.SPOT, agentPoolProfile1.virtualMachinePriority());
        Assertions.assertEquals(ScaleSetEvictionPolicy.DELETE, agentPoolProfile1.virtualMachineEvictionPolicy());
        Assertions.assertEquals(-1.0, agentPoolProfile1.virtualMachineMaximumPrice());

        kubernetesCluster.update()
            .defineAgentPool(agentPoolName2)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withSpotPriorityVirtualMachine(ScaleSetEvictionPolicy.DEALLOCATE)
                .withVirtualMachineMaximumPrice(100.0)
                .attach()
            .apply();

        KubernetesClusterAgentPool agentPoolProfile2 = kubernetesCluster.agentPools().get(agentPoolName2);
        Assertions.assertEquals(ScaleSetPriority.SPOT, agentPoolProfile2.virtualMachinePriority());
        Assertions.assertEquals(ScaleSetEvictionPolicy.DEALLOCATE, agentPoolProfile2.virtualMachineEvictionPolicy());
        Assertions.assertEquals(100.0, agentPoolProfile2.virtualMachineMaximumPrice());
    }

    @Test
    public void canListKubeConfigWithFormat() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);

        // create cluster
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_SOUTH_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .create();

        List<CredentialResult> results = kubernetesCluster.userKubeConfigs(Format.AZURE);
        Assertions.assertFalse(CoreUtils.isNullOrEmpty(results));

        results = kubernetesCluster.userKubeConfigs(null);
        Assertions.assertFalse(CoreUtils.isNullOrEmpty(results));

        byte[] kubeConfigContent1 = kubernetesCluster.userKubeConfigContent(null);
        Assertions.assertTrue(kubeConfigContent1 != null && kubeConfigContent1.length > 0);

        byte[] kubeConfigContent2 = kubernetesCluster.userKubeConfigContent(Format.AZURE);
        Assertions.assertTrue(kubeConfigContent2 != null && kubeConfigContent2.length > 0);

        byte[] kubeConfigContent3 = kubernetesCluster.userKubeConfigContent(Format.EXEC);
        Assertions.assertTrue(kubeConfigContent3 != null && kubeConfigContent3.length > 0);

        // `Format` has no effects on non-aad clusters
        Assertions.assertArrayEquals(kubeConfigContent1, kubeConfigContent2);
        Assertions.assertArrayEquals(kubeConfigContent1, kubeConfigContent3);
    }

    @Test
    public void testKubernetesClusterAzureRbac() {
        final String aksName = generateRandomResourceName("aks", 15);
        final String dnsPrefix = generateRandomResourceName("dns", 10);
        final String agentPoolName = generateRandomResourceName("ap0", 10);

        // Azure AD integration with Azure RBAC
        KubernetesCluster kubernetesCluster = containerServiceManager
            .kubernetesClusters()
            .define(aksName)
            .withRegion(Region.US_WEST3)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withSystemAssignedManagedServiceIdentity()
            .enableAzureRbac()
            .disableLocalAccounts()
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V3)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withOSDiskSizeInGB(30)
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .create();

        Assertions.assertEquals(0, kubernetesCluster.azureActiveDirectoryGroupIds().size());
        Assertions.assertFalse(kubernetesCluster.isLocalAccountsEnabled());
        Assertions.assertTrue(kubernetesCluster.isAzureRbacEnabled());
        Assertions.assertTrue(kubernetesCluster.enableRBAC());
    }

    @Test
    public void canListOrchestrators() {
        List<OrchestratorVersionProfile> profiles = containerServiceManager.kubernetesClusters()
            .listOrchestrators(Region.US_WEST3, ContainerServiceResourceTypes.MANAGED_CLUSTERS)
            .stream().collect(Collectors.toList());
        Assertions.assertFalse(profiles.isEmpty());
        Assertions.assertEquals("Kubernetes", profiles.iterator().next().orchestratorType());
    }

    @Test
    public void testBeginCreateAgentPool() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);

        // create cluster
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_SOUTH_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .create();

        Accepted<AgentPool> acceptedAgentPool = kubernetesCluster.beginCreateAgentPool(agentPoolName1,
            new AgentPoolData()
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.USER)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                .withAgentPoolVirtualMachineCount(1));

        ActivationResponse<AgentPool> activationResponse = acceptedAgentPool.getActivationResponse();
        Assertions.assertEquals("Creating", activationResponse.getStatus().toString());
        Assertions.assertEquals("Creating", activationResponse.getValue().provisioningState());

        Assertions.assertEquals(agentPoolName1, activationResponse.getValue().name());
        Assertions.assertEquals(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS, activationResponse.getValue().type());
        Assertions.assertEquals(AgentPoolMode.USER, activationResponse.getValue().mode());
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_A2_V2, activationResponse.getValue().vmSize());
        Assertions.assertEquals(1, activationResponse.getValue().count());

        AgentPool agentPool = acceptedAgentPool.getFinalResult();
        Assertions.assertEquals("Succeeded", agentPool.provisioningState());
        Assertions.assertEquals(agentPoolName1, agentPool.name());
    }

    @Test
    public void testFipsEnabled() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);
        String agentPoolName2 = generateRandomResourceName("ap2", 10);

        // create cluster
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_SOUTH_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
            .defineAgentPool(agentPoolName1)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.USER)
                .withFipsEnabled() // enable FIPS
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .create();

        Assertions.assertFalse(kubernetesCluster.agentPools().get(agentPoolName).isFipsEnabled());
        Assertions.assertTrue(kubernetesCluster.agentPools().get(agentPoolName1).isFipsEnabled());

        // create a new agent pool with FIPS enabled
        AgentPoolData request = new AgentPoolData()
            .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
            .withAgentPoolVirtualMachineCount(1)
            .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
            .withAgentPoolMode(AgentPoolMode.USER)
            .withFipsEnabled();

        AgentPool agentPool3 = kubernetesCluster.beginCreateAgentPool(agentPoolName2, request).getFinalResult();
        Assertions.assertTrue(agentPool3.isFipsEnabled());

        kubernetesCluster.refresh();
        Assertions.assertTrue(kubernetesCluster.agentPools().get(agentPoolName2).isFipsEnabled());
    }

    @Test
    public void canCreateClusterWithDefaultVersion() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters()
            .define(generateRandomResourceName("aks", 15))
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(generateRandomResourceName("ap", 15))
            .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
            .withAgentPoolVirtualMachineCount(1)
            .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
            .withAgentPoolMode(AgentPoolMode.SYSTEM)
            .attach()
            .withDnsPrefix(generateRandomResourceName("dns", 15))
            .create();

        kubernetesCluster.refresh();
        Assertions.assertNotNull(kubernetesCluster.version());
    }

    @Test
    public void testUpdateKubernetesVersion() {
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters()
            .define(generateRandomResourceName("aks", 15))
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withVersion("1.27.9")
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(generateRandomResourceName("ap", 15))
            .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
            .withAgentPoolVirtualMachineCount(1)
            .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
            .withAgentPoolMode(AgentPoolMode.SYSTEM)
            .attach()
            .withDnsPrefix(generateRandomResourceName("dns", 15))
            .create();

        kubernetesCluster.refresh();
        Assertions.assertEquals("1.27.9", kubernetesCluster.version());

        kubernetesCluster.update().withVersion("1.28.5").apply();
        kubernetesCluster.refresh();
        Assertions.assertEquals("1.28.5", kubernetesCluster.version());
    }

    @Test
    public void testUpdateManagedClusterSkuAndKubernetesSupportPlan() {
        String aksName = generateRandomResourceName("aks", 15);
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters()
            .define(aksName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(generateRandomResourceName("ap", 15))
            .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
            .withAgentPoolVirtualMachineCount(1)
            .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
            .withAgentPoolMode(AgentPoolMode.SYSTEM)
            .attach()
            .withDnsPrefix(generateRandomResourceName("dns", 15))
            .create();

        kubernetesCluster.update().withPremiumSku().apply();
        kubernetesCluster.refresh();
        Assertions.assertEquals(ManagedClusterSkuTier.PREMIUM, kubernetesCluster.sku().tier());
        Assertions.assertEquals(KubernetesSupportPlan.AKSLONG_TERM_SUPPORT, kubernetesCluster.innerModel().supportPlan());

        kubernetesCluster.update().withStandardSku().apply();
        kubernetesCluster.refresh();
        Assertions.assertEquals(ManagedClusterSkuTier.STANDARD, kubernetesCluster.sku().tier());
        Assertions.assertEquals(KubernetesSupportPlan.KUBERNETES_OFFICIAL, kubernetesCluster.innerModel().supportPlan());

        kubernetesCluster.update().withPremiumSku().apply();
        kubernetesCluster.refresh();
        Assertions.assertEquals(ManagedClusterSkuTier.PREMIUM, kubernetesCluster.sku().tier());
        Assertions.assertEquals(KubernetesSupportPlan.AKSLONG_TERM_SUPPORT, kubernetesCluster.innerModel().supportPlan());

        kubernetesCluster.update().withFreeSku().apply();
        kubernetesCluster.refresh();
        Assertions.assertEquals(ManagedClusterSkuTier.FREE, kubernetesCluster.sku().tier());
        Assertions.assertEquals(KubernetesSupportPlan.KUBERNETES_OFFICIAL, kubernetesCluster.innerModel().supportPlan());
    }

    @Test
    public void canCreateKubernetesClusterWithDisablePublicNetworkAccess() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);

        // create
        KubernetesCluster kubernetesCluster =
            containerServiceManager.kubernetesClusters().define(aksName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withDefaultVersion()
                .withRootUsername("testaks")
                .withSshKey(SSH_KEY)
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .disablePublicNetworkAccess()
                .create();

        Assertions.assertEquals(PublicNetworkAccess.DISABLED, kubernetesCluster.publicNetworkAccess());
    }

    @Test
    public void canUpdatePublicNetworkAccess() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);

        // create
        KubernetesCluster kubernetesCluster =
            containerServiceManager.kubernetesClusters().define(aksName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withDefaultVersion()
                .withRootUsername("testaks")
                .withSshKey(SSH_KEY)
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .create();

        kubernetesCluster.update().disablePublicNetworkAccess().apply();
        Assertions.assertEquals(PublicNetworkAccess.DISABLED, kubernetesCluster.publicNetworkAccess());

        kubernetesCluster.update().enablePublicNetworkAccess().apply();
        Assertions.assertEquals(PublicNetworkAccess.ENABLED, kubernetesCluster.publicNetworkAccess());
    }

    @Test
    public void canCreateAndUpdateWithNetworkProperties() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);

        // create
        KubernetesCluster kubernetesCluster =
            containerServiceManager.kubernetesClusters().define(aksName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withDefaultVersion()
                .withRootUsername("testaks")
                .withSshKey(SSH_KEY)
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .attach()
                .defineNetworkProfile()
                .withNetworkPlugin(NetworkPlugin.AZURE)
                .withNetworkPluginMode(NetworkPluginMode.OVERLAY)
                .withNetworkPolicy(NetworkPolicy.AZURE)
                .withNetworkMode(NetworkMode.TRANSPARENT)
                .withNetworkDataPlan(NetworkDataplane.AZURE)
                .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .create();

        kubernetesCluster.refresh();
        Assertions.assertEquals(NetworkPlugin.AZURE, kubernetesCluster.networkProfile().networkPlugin());
        Assertions.assertEquals(NetworkPluginMode.OVERLAY, kubernetesCluster.networkProfile().networkPluginMode());
        Assertions.assertEquals(NetworkPolicy.AZURE, kubernetesCluster.networkProfile().networkPolicy());
        Assertions.assertEquals(NetworkMode.TRANSPARENT, kubernetesCluster.networkProfile().networkMode());
        Assertions.assertEquals(NetworkDataplane.AZURE, kubernetesCluster.networkProfile().networkDataplane());

        kubernetesCluster.update().withNetworkProfile(
            kubernetesCluster.networkProfile()
                .withNetworkPolicy(NetworkPolicy.CILIUM)
                .withNetworkDataplane(NetworkDataplane.CILIUM)
            ).apply();

        kubernetesCluster.refresh();
        Assertions.assertEquals(NetworkPolicy.CILIUM, kubernetesCluster.networkProfile().networkPolicy());
        Assertions.assertEquals(NetworkDataplane.CILIUM, kubernetesCluster.networkProfile().networkDataplane());
    }
}
