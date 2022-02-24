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
import com.azure.resourcemanager.compute.fluent.models.DiskEncryptionSetInner;
import com.azure.resourcemanager.compute.models.DeleteOptions;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetIdentityType;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.compute.models.EncryptionSetIdentity;
import com.azure.resourcemanager.compute.models.KeyForDiskEncryptionSet;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.SourceVault;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class VirtualMachineEncryptionTests extends ResourceManagerTestBase {

    private AzureResourceManager azureResourceManager;

    private String rgName = "";
    private final String vmName = "javavm";
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
    public void canCreateVirtualMachineWithDiskEncryptionSet() {
        String clientId = this.clientIdFromFile();

        // create vault
        String vaultName = generateRandomResourceName("kv", 8);
        Vault vault = azureResourceManager.vaults().define(vaultName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withRoleBasedAccessControl()
            .withPurgeProtectionEnabled()
            .create();

        // RBAC for this app
        azureResourceManager.accessManagement().roleAssignments().define(UUID.randomUUID().toString())
            .forServicePrincipal(clientId)
            .withBuiltInRole(BuiltInRole.KEY_VAULT_ADMINISTRATOR)
            .withResourceScope(vault)
            .create();
        // wait for propagation time
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));

        // create key
        Key key = vault.keys().define("key1")
            .withKeyTypeToCreate(KeyType.RSA)
            .withKeySize(4096)
            .create();

        // create disk encryption set
        DiskEncryptionSetInner diskEncryptionSet = azureResourceManager.disks().manager().serviceClient()
            .getDiskEncryptionSets().createOrUpdate(rgName, "des1", new DiskEncryptionSetInner()
                .withLocation(region.name())
                .withEncryptionType(DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY)
                .withIdentity(new EncryptionSetIdentity().withType(DiskEncryptionSetIdentityType.SYSTEM_ASSIGNED))
                .withActiveKey(new KeyForDiskEncryptionSet()
                    .withSourceVault(new SourceVault().withId(vault.id()))
                    .withKeyUrl(key.id())));

        // RBAC for disk encryption set
        azureResourceManager.accessManagement().roleAssignments().define(UUID.randomUUID().toString())
            .forObjectId(diskEncryptionSet.identity().principalId())
            .withBuiltInRole(BuiltInRole.KEY_VAULT_CRYPTO_SERVICE_ENCRYPTION_USER)
            .withResourceScope(vault)
            .create();
        // wait for propagation time
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));

        // create disk
        Disk disk1 = azureResourceManager.disks().define("disk1")
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withData()
            .withSizeInGB(32)
            .create();

        // create virtual machine
        VirtualMachine vm1 = azureResourceManager.virtualMachines().define(vmName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/27")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("testuser")
            .withSsh(sshPublicKey())
            .withNewDataDisk(16)
            .withExistingDataDisk(disk1)
            .withDataDiskDefaultDeleteOptions(DeleteOptions.DELETE)
            .withDataDiskDefaultEncryptionSet(diskEncryptionSet.id())
            .withOSDiskDeleteOptions(DeleteOptions.DELETE)
            .withOSDiskEncryptionSet(diskEncryptionSet.id())
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        // verification
        Assertions.assertEquals(diskEncryptionSet.id(), vm1.innerModel().storageProfile().osDisk().managedDisk().diskEncryptionSet().id());
        Assertions.assertEquals(diskEncryptionSet.id(), vm1.innerModel().storageProfile().dataDisks().get(0).managedDisk().diskEncryptionSet().id());
        Assertions.assertEquals(diskEncryptionSet.id(), vm1.innerModel().storageProfile().dataDisks().get(1).managedDisk().diskEncryptionSet().id());

        // delete virtual machine
        azureResourceManager.virtualMachines().deleteById(vm1.id());
    }
}
