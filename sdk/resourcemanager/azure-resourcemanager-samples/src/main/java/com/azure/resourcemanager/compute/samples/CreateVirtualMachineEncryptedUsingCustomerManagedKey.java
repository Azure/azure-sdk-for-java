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
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.compute.models.EncryptionType;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.samples.Utils;
import com.azure.security.keyvault.keys.models.KeyType;

/**
 * Azure Compute sample for managing virtual machines -
 * - Create key vault and key
 * - Create disk encryption set
 * - Grant disk encryption set access to the key vault by defining key vault access policy
 * - Create virtual machine with OS disk encrypted using customer managed keys and one data disk, lun1 encrypted using
 *   platform-managed key
 * - Deallocated vm, convert data disk lun1 encryption to customer-managed key, start vm
 * - Create a new disk encrypted using customer-managed key
 * - Attach the disk to the vm as lun2
 */
public final class CreateVirtualMachineEncryptedUsingCustomerManagedKey {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @param clientId clientId of the app
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String clientId) {
        final Region region = Region.US_EAST;
        String rgName = Utils.randomResourceName(azureResourceManager, "rg-", 15);
        final String vaultName = Utils.randomResourceName(azureResourceManager, "vault-", 15);
        final String keyName = Utils.randomResourceName(azureResourceManager, "key-", 15);
        final String desName = Utils.randomResourceName(azureResourceManager, "des-", 15);
        final String vmName = Utils.randomResourceName(azureResourceManager, "vm-", 15);
        final String diskLun3Name = Utils.randomResourceName(azureResourceManager, "disk-", 15);
        final String password = Utils.password();
        final String sshPublicKey = Utils.sshPublicKey();
        try {

            //=============================================================
            // Create key vault and key

            Vault vault = azureResourceManager.vaults()
                .define(vaultName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .defineAccessPolicy()
                    .forServicePrincipal(clientId)
                    .allowKeyPermissions(KeyPermissions.CREATE)
                    .attach()
                .withPurgeProtectionEnabled()
                .create();

            Key vaultKey = vault.keys()
                .define(keyName)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            //=============================================================
            // Create disk encryption set

            DiskEncryptionSet des = azureResourceManager.diskEncryptionSets()
                .define(desName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withEncryptionType(DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY)
                .withExistingKeyVault(vault.id())
                .withExistingKey(vaultKey.id())
                .withSystemAssignedManagedServiceIdentity()
                .withAutomaticKeyRotation()
                .create();

            //=============================================================
            // Grant disk encryption set access to the key vault by defining key vault access policy

            vault.update()
                .defineAccessPolicy()
                    .forObjectId(des.systemAssignedManagedServiceIdentityPrincipalId())
                    .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY)
                    .attach()
                .apply();

            //=============================================================
            // Create virtual machine with OS disk encrypted using customer managed keys and one data disk lun1 encrypted using
            // platform-managed key

            VirtualMachine linuxVM = azureResourceManager.virtualMachines()
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
                .withNewDataDisk(10, 1, CachingTypes.READ_WRITE)
                .withOSDiskDiskEncryptionSet(des.id())
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                .create();
            System.out.println("Created virtual machine, with OS disk encrypted using customer-managed key and a "
                + "data disk lun1 encrypted using platform-managed key: " + linuxVM.id());
            Utils.print(linuxVM);

            //=============================================================
            // Deallocated vm, convert data disk lun1 encryption to customer-managed key, start vm

            linuxVM.deallocate();
            Disk lun1 = azureResourceManager.disks().getById(linuxVM.dataDisks().get(1).id());
            lun1.update()
                .withDiskEncryptionSet(des.id(), EncryptionType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY)
                .apply();
            linuxVM.start();
            linuxVM.refresh();
            System.out.println("Converted encryption of data disk lun2 to customer-managed keys: ");
            Utils.print(linuxVM);

            //=============================================================
            // Create a new disk encrypted using customer-managed key

            Disk lun2 = azureResourceManager.disks().define(diskLun3Name)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withData()
                .withSizeInGB(10)
                .withDiskEncryptionSet(des.id())
                .create();

            //=============================================================
            // Attach the disk to the vm as lun2

            linuxVM.update()
                .withExistingDataDisk(lun2, 2, CachingTypes.READ_WRITE)
                .apply();
            System.out.println("Updated virtual machine with 2 data disks all encrypted using customer-managed key");
            Utils.print(linuxVM);
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

    private CreateVirtualMachineEncryptedUsingCustomerManagedKey() {

    }
}
