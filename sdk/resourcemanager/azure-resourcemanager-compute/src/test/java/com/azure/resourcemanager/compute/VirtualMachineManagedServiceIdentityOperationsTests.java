// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineManagedServiceIdentityOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private final Region region = Region.US_EAST;
    private final String vmName = "javavm";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canSetMSIOnNewOrExistingVMWithoutRoleAssignment() throws Exception {
        // Create a virtual machine with just MSI enabled without role and scope.
        //
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withSystemAssignedManagedServiceIdentity()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNotNull(virtualMachine.innerModel());
        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());

        // Ensure NO role assigned for resource group
        //
        ResourceGroup resourceGroup =
            this.resourceManager.resourceGroups().getByName(virtualMachine.resourceGroupName());
        PagedIterable<RoleAssignment> rgRoleAssignments1 =
            authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        Assertions.assertNotNull(rgRoleAssignments1);
        boolean found = false;
        for (RoleAssignment roleAssignment : rgRoleAssignments1) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertFalse(found, "Resource group should not have a role assignment with virtual machine MSI principal");

        virtualMachine = virtualMachine.update().withSystemAssignedManagedServiceIdentity().apply();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNotNull(virtualMachine.innerModel());
        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());

        // Ensure NO role assigned for resource group
        //
        rgRoleAssignments1 = authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        Assertions.assertNotNull(rgRoleAssignments1);
        found = false;
        for (RoleAssignment roleAssignment : rgRoleAssignments1) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertFalse(found, "Resource group should not have a role assignment with virtual machine MSI principal");
    }

    @Test
    public void canSetMSIOnNewVMWithRoleAssignedToCurrentResourceGroup() throws Exception {
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNotNull(virtualMachine.innerModel());
        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());

        // Validate service created service principal
        // TODO: Renable the below code snippet: https://github.com/Azure/azure-libraries-for-net/issues/739
        //        ServicePrincipal servicePrincipal = authorizationManager
        //                .servicePrincipals()
        //                .getById(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        //
        //        Assertions.assertNotNull(servicePrincipal);
        //        Assertions.assertNotNull(servicePrincipal.inner());

        // Ensure role assigned
        //
        ResourceGroup resourceGroup =
            this.resourceManager.resourceGroups().getByName(virtualMachine.resourceGroupName());
        PagedIterable<RoleAssignment> rgRoleAssignments = authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        boolean found = false;
        for (RoleAssignment rgRoleAssignment : rgRoleAssignments) {
            if (rgRoleAssignment.principalId() != null
                && rgRoleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }

        Assertions.assertTrue(found, "Resource group should have a role assignment with virtual machine MSI principal");
    }

    @Test
    public void canSetMSIOnNewVMWithMultipleRoleAssignments() throws Exception {
        String storageAccountName = generateRandomResourceName("javacsrg", 15);

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(storageAccountName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .create();

        ResourceGroup resourceGroup =
            this.resourceManager.resourceGroups().getByName(storageAccount.resourceGroupName());

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessTo(resourceGroup.id(), BuiltInRole.CONTRIBUTOR)
                .withSystemAssignedIdentityBasedAccessTo(storageAccount.id(), BuiltInRole.CONTRIBUTOR)
                .create();

        // Validate service created service principal
        //

        // TODO: Renable the below code snippet: https://github.com/Azure/azure-libraries-for-net/issues/739

        //        ServicePrincipal servicePrincipal = authorizationManager
        //                .servicePrincipals()
        //                .getById(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        //
        //        Assertions.assertNotNull(servicePrincipal);
        //        Assertions.assertNotNull(servicePrincipal.inner());

        // Ensure role assigned for resource group
        //
        PagedIterable<RoleAssignment> rgRoleAssignments = authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        Assertions.assertNotNull(rgRoleAssignments);
        boolean found = false;
        for (RoleAssignment roleAssignment : rgRoleAssignments) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Resource group should have a role assignment with virtual machine MSI principal");

        // Ensure role assigned for storage account
        //
        PagedIterable<RoleAssignment> stgRoleAssignments =
            authorizationManager.roleAssignments().listByScope(storageAccount.id());
        Assertions.assertNotNull(stgRoleAssignments);
        found = false;
        for (RoleAssignment roleAssignment : stgRoleAssignments) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(found, "Storage account should have a role assignment with virtual machine MSI principal");
    }

    @Test
    public void canSetMSIOnExistingVMWithRoleAssignments() throws Exception {
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withSystemAssignedManagedServiceIdentity()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNotNull(virtualMachine.innerModel());
        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());

        Assertions.assertNotNull(virtualMachine.managedServiceIdentityType());
        Assertions.assertTrue(virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED));

        // Ensure NO role assigned for resource group
        //
        ResourceGroup resourceGroup =
            this.resourceManager.resourceGroups().getByName(virtualMachine.resourceGroupName());
        PagedIterable<RoleAssignment> rgRoleAssignments1 =
            authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        Assertions.assertNotNull(rgRoleAssignments1);
        boolean found = false;
        for (RoleAssignment roleAssignment : rgRoleAssignments1) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertFalse(found, "Resource group should not have a role assignment with virtual machine MSI principal");

        virtualMachine
            .update()
            .withSystemAssignedManagedServiceIdentity()
            .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
            .apply();

        // Ensure role assigned for resource group
        //
        PagedIterable<RoleAssignment> roleAssignments2 = authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        Assertions.assertNotNull(roleAssignments2);
        for (RoleAssignment roleAssignment : roleAssignments2) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Resource group should have a role assignment with virtual machine MSI principal");
    }

    private static Integer objectToInteger(Object obj) {
        Integer result = null;
        if (obj != null) {
            if (obj instanceof Integer) {
                result = (Integer) obj;
            } else {
                result = Integer.valueOf((String) obj);
            }
        }
        return result;
    }
}
