// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.core.management.Region;
import com.azure.resourcemanager.samples.Utils;

/**
 * Azure Compute sample for managing virtual machine from Managed Service Identity (MSI) enabled virtual machine -
 *   - Create a virtual machine using MSI credentials from System assigned or User Assigned MSI enabled VM.
 */
public final class ManageVirtualMachineFromMSIEnabledVirtualMachine {
    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        final Region region = Region.US_WEST_CENTRAL;

        // This sample required to be run from a ManagedIdentityCredential (User Assigned or System Assigned) enabled virtual
        // machine with role based contributor access to the resource group specified as the second command line argument.
        //
        // see https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
        //

        final String usage = "Usage: mvn clean compile exec:java -Dexec.args=\"<subscription-id> <rg-name> [<client-id>] [<cleanup-resource>]\"";
        if (args.length < 2) {
            throw new IllegalArgumentException(usage);
        }

        final String subscriptionId = args[0];
        final String resourceGroupName = args[1];
        final String clientId = args.length > 2 ? args[2] : null;
        final boolean cleanupResource = args.length <= 3 || Boolean.getBoolean(args[3]);
        final String linuxVMName = "yourVirtualMachineName";
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();

        //=============================================================
        // ManagedIdentityCredential Authenticate

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
            .clientId(clientId)
            .build();

        AzureProfile profile = new AzureProfile(null, subscriptionId, AzureEnvironment.AZURE);

        AzureResourceManager azure = AzureResourceManager.configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withSubscription(subscriptionId);

        // Print selected subscription
        System.out.println("Selected subscription: " + azure.subscriptionId());

        //=============================================================
        // Create a Linux VM using MSI credentials

        System.out.println("Creating a Linux VM using ManagedIdentityCredential.");

        VirtualMachine virtualMachine = azure.virtualMachines()
            .define(linuxVMName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroupName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername(userName)
            .withSsh(sshPublicKey)
            .create();

        System.out.println("Created virtual machine using ManagedIdentityCredential.");
        Utils.print(virtualMachine);

        if (cleanupResource) {
            System.out.println("Deleting resource group: " + resourceGroupName);
            azure.resourceGroups().deleteByName(resourceGroupName);
            System.out.println("Deleted resource group: " + resourceGroupName);
        }
    }

    private ManageVirtualMachineFromMSIEnabledVirtualMachine() {
    }
}
