/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class VirtualMachineManagedServiceIdentityOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static final Region REGION = Region.US_SOUTH_CENTRAL;
    private static final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
    }

    @Test
    public void canEnableMSIOnNewVirtualMachineWithCurrentResourceGroupScope() throws Exception {
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withManagedServiceIdentity()
                .create();

        Assert.assertNotNull(virtualMachine);
        Assert.assertNotNull(virtualMachine.inner());
        Assert.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assert.assertNotNull(virtualMachine.managedServiceIdentityPrincipalId());
        Assert.assertNotNull(virtualMachine.managedServiceIdentityTenantId());

        // Validate service created service principal
        //
        ServicePrincipal servicePrincipal = rbacManager
                .servicePrincipals()
                .getById(virtualMachine.managedServiceIdentityPrincipalId());

        Assert.assertNotNull(servicePrincipal);
        Assert.assertNotNull(servicePrincipal.inner());

        // Ensure the MSI extension is set
        //
        Map<String, VirtualMachineExtension> extensions = virtualMachine.listExtensions();
        boolean extensionFound = false;
        for (VirtualMachineExtension extension : extensions.values()) {
            if (extension.publisherName().equalsIgnoreCase("Microsoft.ManagedIdentity")
                    && extension.typeName().equalsIgnoreCase("ManagedIdentityExtensionForLinux")) {
                extensionFound = true;
                break;
            }
        }
        Assert.assertTrue(extensionFound);
    }

    @Test
    public void canEnableMSIOnNewVirtualMachineWithStorageAccountScope() throws Exception {
        String storageAccountName = generateRandomResourceName("javacsmrg", 15);

        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(storageAccountName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(RG_NAME)
                .create();

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withManagedServiceIdentity(BuiltInRole.CONTRIBUTOR, storageAccount.id())
                .create();

        // Validate service created service principal
        //
        ServicePrincipal servicePrincipal = rbacManager
                .servicePrincipals()
                .getById(virtualMachine.managedServiceIdentityPrincipalId());

        Assert.assertNotNull(servicePrincipal);
        Assert.assertNotNull(servicePrincipal.inner());

        // Ensure the MSI extension is set
        //
        Map<String, VirtualMachineExtension> extensions = virtualMachine.listExtensions();
        boolean extensionFound = false;
        for (VirtualMachineExtension extension : extensions.values()) {
            if (extension.publisherName().equalsIgnoreCase("Microsoft.ManagedIdentity")
                    && extension.typeName().equalsIgnoreCase("ManagedIdentityExtensionForLinux")) {
                extensionFound = true;
                break;
            }
        }
        Assert.assertTrue(extensionFound);
    }

    @Test
    public void canEnableMSIOnExistingVirtualMachineWithCurrentResourceGroupScope() throws Exception  {
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withManagedServiceIdentity()
                .create();

        virtualMachine.update()
                .withManagedServiceIdentity()
                .apply();

        Assert.assertNotNull(virtualMachine);
        Assert.assertNotNull(virtualMachine.inner());
        Assert.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assert.assertNotNull(virtualMachine.managedServiceIdentityPrincipalId());
        Assert.assertNotNull(virtualMachine.managedServiceIdentityTenantId());

        // Ensure the MSI extension is set
        //
        Map<String, VirtualMachineExtension> extensions = virtualMachine.listExtensions();
        boolean extensionFound = false;
        for (VirtualMachineExtension extension : extensions.values()) {
            if (extension.publisherName().equalsIgnoreCase("Microsoft.ManagedIdentity")
                    && extension.typeName().equalsIgnoreCase("ManagedIdentityExtensionForLinux")) {
                extensionFound = true;
                break;
            }
        }
        Assert.assertTrue(extensionFound);
    }
}
