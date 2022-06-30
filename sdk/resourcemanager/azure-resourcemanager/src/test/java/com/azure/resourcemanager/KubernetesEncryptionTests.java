// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

public class KubernetesEncryptionTests extends DiskEncryptionTestBase {

    @Test
    public void canCreateClusterWithDiskEncryption() {
        final String clientId = this.clientIdFromFile();

        // create vault and key
        final String vaultName = generateRandomResourceName("kv", 8);
        VaultAndKey vaultAndKey = createVaultAndKey(vaultName, clientId);

        // create disk encryption set
        DiskEncryptionSet diskEncryptionSet = createDiskEncryptionSet("des1",
            DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS, vaultAndKey);

        final String aksName = generateRandomResourceName("aks", 15);
        final String dnsPrefix = generateRandomResourceName("dns", 10);
        final String agentPoolName = generateRandomResourceName("ap0", 10);

        // create
        KubernetesCluster kubernetesCluster = azureResourceManager
            .kubernetesClusters()
            .define(aksName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withDefaultVersion()
            .withSystemAssignedManagedServiceIdentity()
            .disableLocalAccounts()
            .withDiskEncryptionSet(diskEncryptionSet.id())
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V3)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withOSDiskSizeInGB(30)
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .create();

        Assertions.assertEquals(diskEncryptionSet.id().toLowerCase(Locale.ROOT), kubernetesCluster.diskEncryptionSetId().toLowerCase(Locale.ROOT));
    }
}
