// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.management.Region;
import com.azure.resourcemanager.containerservice.models.Code;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import com.azure.resourcemanager.containerservice.models.PowerState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MockSdkSamples {

    @Test
    public void mockResponse() {
        AzureResourceManager mockAzure = Mockito.mock(AzureResourceManager.class);
        KubernetesClusters mockClusters = Mockito.mock(KubernetesClusters.class);
        KubernetesCluster mockCluster = Mockito.mock(KubernetesCluster.class);

        PowerState stubPowerState = new PowerState().withCode(Code.RUNNING);

        Mockito.when(mockAzure.kubernetesClusters()).thenReturn(mockClusters);
        Mockito.when(mockClusters.getById(Mockito.anyString())).thenReturn(mockCluster);
        Mockito.when(mockCluster.powerState()).thenReturn(stubPowerState);

        Assertions.assertEquals(Code.RUNNING, mockAzure.kubernetesClusters().getById("mockId").powerState().code());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void mockCreate() {
        AzureResourceManager mockAzure = Mockito.mock(AzureResourceManager.class);
        KubernetesClusters mockClusters = Mockito.mock(KubernetesClusters.class);
        KubernetesCluster mockCluster = Mockito.mock(KubernetesCluster.class);

        PowerState stubPowerState = new PowerState().withCode(Code.RUNNING);

        /*
        The most desirable approach for mocking Fluent API would be the “Mockito.RETURNS_DEEP_STUBS” provided by Mockito.

        However, some of the interfaces involved in our API uses generic, and due to type erasure Mockito will not able to handle them.
        Hence, we still need to use the full declaration on every step of the Fluent API.
         */
        KubernetesCluster.DefinitionStages.Blank mockStage1 = Mockito.mock(KubernetesCluster.DefinitionStages.Blank.class);
        KubernetesCluster.DefinitionStages.WithGroup mockStage2 = Mockito.mock(KubernetesCluster.DefinitionStages.WithGroup.class);
        KubernetesCluster.DefinitionStages.WithVersion mockStage3 = Mockito.mock(KubernetesCluster.DefinitionStages.WithVersion.class);
        KubernetesCluster.DefinitionStages.WithLinuxRootUsername mockStage4 = Mockito.mock(KubernetesCluster.DefinitionStages.WithLinuxRootUsername.class);
        KubernetesCluster.DefinitionStages.WithLinuxSshKey mockStage5 = Mockito.mock(KubernetesCluster.DefinitionStages.WithLinuxSshKey.class);
        KubernetesCluster.DefinitionStages.WithServicePrincipalClientId mockStage6 = Mockito.mock(KubernetesCluster.DefinitionStages.WithServicePrincipalClientId.class);
        KubernetesCluster.DefinitionStages.WithCreate mockStage7 = Mockito.mock(KubernetesCluster.DefinitionStages.WithCreate.class);
        KubernetesClusterAgentPool.DefinitionStages.Blank mockStage8 = Mockito.mock(KubernetesClusterAgentPool.DefinitionStages.Blank.class);
        KubernetesClusterAgentPool.DefinitionStages.WithAgentPoolVirtualMachineCount mockStage9 = Mockito.mock(KubernetesClusterAgentPool.DefinitionStages.WithAgentPoolVirtualMachineCount.class);
        KubernetesClusterAgentPool.DefinitionStages.WithAttach mockStage10 = Mockito.mock(KubernetesClusterAgentPool.DefinitionStages.WithAttach.class);

        Mockito.when(mockAzure.kubernetesClusters()).thenReturn(mockClusters);

        Mockito.when(mockClusters.define(Mockito.anyString())).thenReturn(mockStage1);
        Mockito.when(mockStage1.withRegion(Mockito.any(Region.class))).thenReturn(mockStage2);
        Mockito.when(mockStage2.withExistingResourceGroup(Mockito.anyString())).thenReturn(mockStage3);
        Mockito.when(mockStage3.withDefaultVersion()).thenReturn(mockStage4);
        Mockito.when(mockStage4.withRootUsername(Mockito.anyString())).thenReturn(mockStage5);
        Mockito.when(mockStage5.withSshKey(Mockito.anyString())).thenReturn(mockStage6);
        Mockito.when(mockStage6.withSystemAssignedManagedServiceIdentity()).thenReturn(mockStage7);
        Mockito.when(mockStage7.create()).thenReturn(mockCluster);
        Mockito.when(mockStage7.defineAgentPool(Mockito.anyString())).thenReturn(mockStage8);
        Mockito.when(mockStage8.withVirtualMachineSize(Mockito.any())).thenReturn(mockStage9);
        Mockito.when(mockStage9.withAgentPoolVirtualMachineCount(Mockito.anyInt())).thenReturn(mockStage10);
        Mockito.when(mockStage10.attach()).thenReturn(mockStage7);

        Mockito.when(mockCluster.powerState()).thenReturn(stubPowerState);

        KubernetesCluster cluster = mockAzure.kubernetesClusters().define("aks1")
            .withRegion(Region.US_EAST2)
            .withExistingResourceGroup("rg1")
            .withDefaultVersion()
            .withRootUsername("user")
            .withSshKey("key")
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool("ap1")
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D1_V2)
                .withAgentPoolVirtualMachineCount(3)
                .attach()
            .create();

        Assertions.assertEquals(Code.RUNNING, cluster.powerState().code());
    }
}
