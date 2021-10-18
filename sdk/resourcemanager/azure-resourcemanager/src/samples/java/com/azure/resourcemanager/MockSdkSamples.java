// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.containerservice.models.Code;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import com.azure.resourcemanager.containerservice.models.PowerState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MockSdkSamples {

    @Test
    public void mockSimpleResponse() {
        AzureResourceManager mockAzure = Mockito.mock(AzureResourceManager.class);
        KubernetesClusters mockClusters = Mockito.mock(KubernetesClusters.class);
        KubernetesCluster mockCluster = Mockito.mock(KubernetesCluster.class);

        PowerState stubPowerState = new PowerState().withCode(Code.RUNNING);

        Mockito.when(mockAzure.kubernetesClusters()).thenReturn(mockClusters);
        Mockito.when(mockClusters.getById(Mockito.anyString())).thenReturn(mockCluster);
        Mockito.when(mockCluster.powerState()).thenReturn(stubPowerState);

        Assertions.assertEquals(Code.RUNNING, mockAzure.kubernetesClusters().getById("mockId").powerState().code());
    }
}
