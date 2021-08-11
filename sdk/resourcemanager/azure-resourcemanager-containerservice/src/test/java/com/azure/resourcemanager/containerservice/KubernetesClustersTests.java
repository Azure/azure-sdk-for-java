// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.Code;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubeletDiskType;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.core.management.Region;
import com.azure.resourcemanager.containerservice.models.ManagedClusterPropertiesAutoScalerProfile;
import com.azure.resourcemanager.containerservice.models.OSDiskType;
import com.azure.resourcemanager.containerservice.models.ScaleSetEvictionPolicy;
import com.azure.resourcemanager.containerservice.models.ScaleSetPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map;


public class KubernetesClustersTests extends ContainerServiceManagementTest {
    private static final String SSH_KEY = sshPublicKey();

    @Test
    public void canCRUDKubernetesCluster() throws Exception {
        // enable preview feature of ACR Teleport for AKS
        Context context = new Context(
            AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY,
            new HttpHeaders().set("EnableACRTeleport", "true"));

        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);
        String agentPoolName2 = generateRandomResourceName("ap2", 10);
        String servicePrincipalClientId = "spId";
        String servicePrincipalSecret = "spSecret";

        // aks can use another azure auth rather than original client auth to access azure service.
        // Thus, set it to AZURE_AUTH_LOCATION_2 when you want.
        String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");
        if (envSecondaryServicePrincipal == null
            || envSecondaryServicePrincipal.isEmpty()
            || !(new File(envSecondaryServicePrincipal).exists())) {
            envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
        }

        if (!isPlaybackMode()) {
            HashMap<String, String> credentialsMap = parseAuthFile(envSecondaryServicePrincipal);
            servicePrincipalClientId = credentialsMap.get("clientId");
            servicePrincipalSecret = credentialsMap.get("clientSecret");
        }

        /*
        KubeletDiskType requires registering following preview feature:
            azure.features().register("Microsoft.ContainerService", "KubeletDisk");
         */

        // create
        KubernetesCluster kubernetesCluster =
            containerServiceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(Region.US_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withDefaultVersion()
                .withRootUsername("testaks")
                .withSshKey(SSH_KEY)
                .withServicePrincipalClientId(servicePrincipalClientId)
                .withServicePrincipalSecret(servicePrincipalSecret)
                .defineAgentPool(agentPoolName)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_F4S_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .withOSDiskSizeInGB(30)
                    .withOSDiskType(OSDiskType.EPHEMERAL)
                    .withKubeletDiskType(KubeletDiskType.TEMPORARY)
                    .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .attach()
                .defineAgentPool(agentPoolName1)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .withTag("tag1", "value1")
                .create(context);

        Assertions.assertNotNull(kubernetesCluster.id());
        Assertions.assertEquals(Region.US_CENTRAL, kubernetesCluster.region());
        Assertions.assertEquals("testaks", kubernetesCluster.linuxRootUsername());
        Assertions.assertEquals(2, kubernetesCluster.agentPools().size());

        KubernetesClusterAgentPool agentPool = kubernetesCluster.agentPools().get(agentPoolName);
        Assertions.assertNotNull(agentPool);
        Assertions.assertEquals(1, agentPool.count());
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_F4S_V2, agentPool.vmSize());
        Assertions.assertEquals(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS, agentPool.type());
        Assertions.assertEquals(AgentPoolMode.SYSTEM, agentPool.mode());
        Assertions.assertEquals(OSDiskType.EPHEMERAL, agentPool.osDiskType());
        Assertions.assertEquals(30, agentPool.osDiskSizeInGB());
        Assertions.assertEquals(KubeletDiskType.TEMPORARY, agentPool.kubeletDiskType());

        agentPool = kubernetesCluster.agentPools().get(agentPoolName1);
        Assertions.assertNotNull(agentPool);
        Assertions.assertEquals(1, agentPool.count());
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_A2_V2, agentPool.vmSize());
        Assertions.assertEquals(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS, agentPool.type());

        Assertions.assertNotNull(kubernetesCluster.tags().get("tag1"));

        // stop
        kubernetesCluster.stop();
        kubernetesCluster.refresh();
        Assertions.assertEquals(Code.STOPPED, kubernetesCluster.powerState().code());

        // start
        kubernetesCluster.start();
        kubernetesCluster.refresh();
        Assertions.assertEquals(Code.RUNNING, kubernetesCluster.powerState().code());

        // update
        kubernetesCluster =
            kubernetesCluster
                .update()
                .updateAgentPool(agentPoolName1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .withAgentPoolVirtualMachineCount(2)
                    .withKubeletDiskType(KubeletDiskType.OS)
                    .parent()
                .defineAgentPool(agentPoolName2)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                    .withAgentPoolVirtualMachineCount(1)
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

        agentPool = kubernetesCluster.agentPools().get(agentPoolName2);
        Assertions.assertNotNull(agentPool);
        Assertions.assertEquals(ContainerServiceVMSizeTypes.STANDARD_A2_V2, agentPool.vmSize());
        Assertions.assertEquals(1, agentPool.count());

        Assertions.assertEquals("value2", kubernetesCluster.tags().get("tag2"));
        Assertions.assertFalse(kubernetesCluster.tags().containsKey("tag1"));
    }

    @Test
    public void canAutoScaleKubernetesCluster() throws Exception {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);
        String agentPoolName2 = generateRandomResourceName("ap2", 10);
        String agentPoolName3 = generateRandomResourceName("ap2", 10);
        String servicePrincipalClientId = "spId";
        String servicePrincipalSecret = "spSecret";

        // aks can use another azure auth rather than original client auth to access azure service.
        // Thus, set it to AZURE_AUTH_LOCATION_2 when you want.
        String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");
        if (envSecondaryServicePrincipal == null
            || envSecondaryServicePrincipal.isEmpty()
            || !(new File(envSecondaryServicePrincipal).exists())) {
            envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
        }

        if (!isPlaybackMode()) {
            HashMap<String, String> credentialsMap = parseAuthFile(envSecondaryServicePrincipal);
            servicePrincipalClientId = credentialsMap.get("clientId");
            servicePrincipalSecret = credentialsMap.get("clientSecret");
        }

        Map<String, String> nodeLables = new HashMap<>(2);
        nodeLables.put("environment", "dev");
        nodeLables.put("app.1", "spring");

        List<String> nodeTaints = new ArrayList<>(1);
        nodeTaints.add("key=value:NoSchedule");

        // create cluster
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("testaks")
            .withSshKey(SSH_KEY)
            .withServicePrincipalClientId(servicePrincipalClientId)
            .withServicePrincipalSecret(servicePrincipalSecret)
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
        System.out.println(new String(kubernetesCluster.adminKubeConfigContent(), StandardCharsets.UTF_8));

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
    public void canCreateClusterWithSpotVM() throws Exception {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);
        String agentPoolName2 = generateRandomResourceName("ap2", 10);

        // create cluster
        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_CENTRAL)
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
        System.out.println(new String(kubernetesCluster.adminKubeConfigContent(), StandardCharsets.UTF_8));

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

    /**
     * Parse azure auth to hashmap
     *
     * @param authFilename the azure auth location
     * @return all fields in azure auth json
     * @throws Exception exception
     */
    private static HashMap<String, String> parseAuthFile(String authFilename) throws Exception {
        String content = new String(Files.readAllBytes(new File(authFilename).toPath()), StandardCharsets.UTF_8).trim();
        HashMap<String, String> auth = new HashMap<>();
        if (isJsonBased(content)) {
            auth = new JacksonAdapter().deserialize(content, auth.getClass(), SerializerEncoding.JSON);
        } else {
            Properties authSettings = new Properties();
            FileInputStream credentialsFileStream = new FileInputStream(new File(authFilename));
            authSettings.load(credentialsFileStream);
            credentialsFileStream.close();

            for (final String authName : authSettings.stringPropertyNames()) {
                auth.put(authName, authSettings.getProperty(authName));
            }
        }
        return auth;
    }

    private static boolean isJsonBased(String content) {
        return content.startsWith("{");
    }
}
