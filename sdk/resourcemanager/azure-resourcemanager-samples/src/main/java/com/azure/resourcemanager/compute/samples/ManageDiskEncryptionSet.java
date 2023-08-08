// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDiskOptions;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.security.keyvault.keys.models.KeyType;

import java.time.Duration;

/**
 * Azure Compute sample for managing disk encryption sets -
 *  - Create a key vault kv1 and key k1
 *  - Create a disk encryption set, des1, with key vault kv1, key k1 and encryption type
 *     ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY
 *  - Grant the disk encryption set access to the key vault by defining key vault access policy
 *  - Create a new key vault kv2 with RBAC enabled and key k2
 *  - Create a new disk encryption set des2 with key vault kv2, key k2, encryption type
 *     ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS and grant it role-based access to kv2
 *  - Create a virtual machine, with os disk encrypted by des1 and a data disk encrypted by des2
 */
public final class ManageDiskEncryptionSet {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @param clientId clientId of the app
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String clientId) {
        final Region region = Region.US_WEST;
        String rgName = Utils.randomResourceName(azureResourceManager, "rg", 15);
        String kv1Name = Utils.randomResourceName(azureResourceManager, "kv", 15);
        String k1Name = Utils.randomResourceName(azureResourceManager, "k", 15);
        String des1Name = Utils.randomResourceName(azureResourceManager, "des", 15);
        String kv2Name = Utils.randomResourceName(azureResourceManager, "kv", 15);
        String rbacName = Utils.randomUuid(azureResourceManager);
        String k2Name = Utils.randomResourceName(azureResourceManager, "k", 15);
        String des2Name = Utils.randomResourceName(azureResourceManager, "des", 15);
        String vmName = Utils.randomResourceName(azureResourceManager, "vm", 15);

        final String password = Utils.password();
        final String sshPublicKey = Utils.sshPublicKey();
        try {

            //=============================================================
            // Create a key vault kv1 and key k1

            Vault kv1 = azureResourceManager.vaults()
                .define(kv1Name)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .defineAccessPolicy()
                    .forServicePrincipal(clientId)
                    .allowKeyPermissions(KeyPermissions.CREATE)
                    .attach()
                .withPurgeProtectionEnabled()
                .create();

            Key k1 = kv1.keys()
                .define(k1Name)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            //=============================================================
            // Create a disk encryption set, des1, with key vault kv1, key k1 and encryption type
            // ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY

            DiskEncryptionSet des1 = azureResourceManager.diskEncryptionSets()
                .define(des1Name)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withEncryptionType(DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY)
                .withExistingKeyVault(kv1.id())
                .withExistingKey(k1.id())
                .withSystemAssignedManagedServiceIdentity()
                .withAutomaticKeyRotation()
                .create();

            //=============================================================
            // Grant the disk encryption set access to the key vault by defining key vault access policy

            kv1.update()
                .defineAccessPolicy()
                    .forObjectId(des1.systemAssignedManagedServiceIdentityPrincipalId())
                    .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY)
                    .attach()
                .apply();

            //=============================================================
            // Create a new key vault kv2 with RBAC enabled and key k2

            Vault kv2 = azureResourceManager.vaults()
                .define(kv2Name)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withRoleBasedAccessControl()
                .withPurgeProtectionEnabled()
                .create();

            azureResourceManager.accessManagement().roleAssignments().define(rbacName)
                .forServicePrincipal(clientId)
                .withBuiltInRole(BuiltInRole.KEY_VAULT_ADMINISTRATOR)
                .withResourceScope(kv2)
                .create();
            // wait for propagation time
            ResourceManagerUtils.sleep(Duration.ofMinutes(1));

            Key k2 = kv2.keys()
                .define(k2Name)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            //=============================================================
            // Create a new disk encryption set des2 with key vault kv2, key k2, encryption type
            // ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS and grant it role-based access to kv2

            DiskEncryptionSet des2 = azureResourceManager.diskEncryptionSets()
                .define(des2Name)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withEncryptionType(DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS)
                .withExistingKeyVault(kv2.id())
                .withExistingKey(k2.id())
                .withSystemAssignedManagedServiceIdentity()
                .withRoleBasedAccessToCurrentKeyVault()
                .withAutomaticKeyRotation()
                .create();

            //=============================================================
            // Create a virtual machine, with os disk encrypted by des1 and a data disk encrypted by des2

            VirtualMachine vm = azureResourceManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword(password)
                .withSsh(sshPublicKey)
                .withNewDataDisk(10, 1, new VirtualMachineDiskOptions().withDiskEncryptionSet(des2.id()))
                .withOSDiskDiskEncryptionSet(des1.id())
                .create();
            System.out.println("Created virtual machine encrypted using customer managed keys: " + vm.id());
            Utils.print(vm);
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager,
                Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageDiskEncryptionSet() {

    }
}
