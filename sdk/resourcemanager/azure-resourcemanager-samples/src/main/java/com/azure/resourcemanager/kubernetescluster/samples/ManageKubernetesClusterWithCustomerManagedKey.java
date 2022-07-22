// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.kubernetescluster.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.samples.Utils;
import com.azure.security.keyvault.keys.models.KeyType;

/**
 * Azure Container Service (AKS) sample for managing a Kubernetes cluster with customer-managed key.
 *   - Create a key vault with purge-protection enabled
 *   - Create a key in key vault
 *   - Create a disk encryption set using the created key as the encryption key with automatic key-rotation
 *   - Grant the des access to the key vault
 *   - Create an Azure Container Service (AKS) with managed Kubernetes cluster, os disk encrypted using customer-managed key
 */
public class ManageKubernetesClusterWithCustomerManagedKey {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param clientId clientId of the app
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String clientId) {
        final String vaultName = Utils.randomResourceName(azureResourceManager, "v", 15);
        final String keyName = Utils.randomResourceName(azureResourceManager, "vk", 15);
        final String desName = Utils.randomResourceName(azureResourceManager, "des", 15);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgaks", 15);
        final String aksName = Utils.randomResourceName(azureResourceManager, "akssample", 30);
        final Region region = Region.US_EAST;

        try {
            //=============================================================
            // Create a key vault with purge-protection enabled

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

            System.out.println("Created key vault: " + vault.name());

            //=============================================================
            // Create a key in key vault

            Key vaultKey = vault.keys()
                .define(keyName)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            System.out.println("Created key vault key: " + vaultKey.id());

            //=============================================================
            // Create a disk encryption set using the created key as the encryption key with automatic key-rotation
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

            System.out.println("Created disk encryption set with automatic key-rotation: " + des.name());

            //=============================================================
            // Grant the des access to the key vault

            vault.update()
                .defineAccessPolicy()
                    .forObjectId(des.systemAssignedManagedServiceIdentityPrincipalId())
                    .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY)
                    .attach()
                .apply();

            System.out.println("Granted des access to the key vault.");

            //=============================================================
            // Create an Azure Container Service (AKS) with managed Kubernetes cluster, os disk encrypted using customer-managed key

            KubernetesCluster kubernetesCluster = azureResourceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withDefaultVersion()
                .withSystemAssignedManagedServiceIdentity()
                .withDiskEncryptionSet(des.id())
                .defineAgentPool("agentpool")
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V3)
                    .withAgentPoolVirtualMachineCount(1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .withOSDiskSizeInGB(30)
                    .attach()
                .withDnsPrefix("mp1" + aksName)
                .create();

            System.out.println("Created Azure Container Service (AKS) with managed Kubernetes cluster, "
                + "os disk encrypted using customer-managed key");
            Utils.print(kubernetesCluster);

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
     *
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
}
