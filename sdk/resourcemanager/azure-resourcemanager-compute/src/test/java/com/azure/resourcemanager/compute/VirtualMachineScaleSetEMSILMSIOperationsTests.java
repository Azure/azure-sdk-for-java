// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class VirtualMachineScaleSetEMSILMSIOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private Region region = Region.fromName("West Central US");
    private final String vmssName = "javavmss";

    @Override
    protected void cleanUpResources() {
        this.resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateUpdateVirtualMachineScaleSetWithEMSI() throws Exception {
        rgName = generateRandomResourceName("java-ems-c-rg", 15);
        String identityName1 = generateRandomResourceName("msi-id", 15);
        String identityName2 = generateRandomResourceName("msi-id", 15);
        String networkName = generateRandomResourceName("nw", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Create a virtual network to which we will assign "EMSI" with reader access
        //
        Network network =
            networkManager
                .networks()
                .define(networkName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .create();

        // Create an "User Assigned (External) MSI" residing in the above RG and assign reader access to the virtual
        // network
        //
        Identity createdIdentity =
            msiManager
                .identities()
                .define(identityName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAccessTo(network, BuiltInRole.READER)
                .create();

        // Prepare a definition for yet-to-be-created "User Assigned (External) MSI" with contributor access to the
        // resource group
        // it resides
        //
        Creatable<Identity> creatableIdentity =
            msiManager
                .identities()
                .define(identityName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);

        // Create a virtual network for VMSS
        //
        Network vmssNetwork =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        // Create a Load balancer for VMSS
        //
        LoadBalancer vmssInternalLoadBalancer = createInternalLoadBalancer(region, resourceGroup, vmssNetwork, "1");

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(vmssNetwork, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withExistingPrimaryInternalLoadBalancer(vmssInternalLoadBalancer)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withExistingUserAssignedManagedServiceIdentity(createdIdentity)
                .withNewUserAssignedManagedServiceIdentity(creatableIdentity)
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertNotNull(virtualMachineScaleSet.innerModel());
        Assertions.assertTrue(virtualMachineScaleSet.isManagedServiceIdentityEnabled());
        Assertions
            .assertNull(
                virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId()); // No Local MSI enabled
        Assertions
            .assertNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityTenantId()); // No Local MSI enabled

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine scale set
        //
        Set<String> emsiIds = virtualMachineScaleSet.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(2, emsiIds.size());

        // Ensure the "User Assigned (External) MSI"s matches with the those provided as part of VMSS create
        //
        Identity implicitlyCreatedIdentity = null;
        for (String emsiId : emsiIds) {
            Identity identity = msiManager.identities().getById(emsiId);
            Assertions.assertNotNull(identity);
            Assertions
                .assertTrue(
                    identity.name().equalsIgnoreCase(identityName1) || identity.name().equalsIgnoreCase(identityName2));
            Assertions.assertNotNull(identity.principalId());

            if (identity.name().equalsIgnoreCase(identityName2)) {
                implicitlyCreatedIdentity = identity;
            }
        }
        Assertions.assertNotNull(implicitlyCreatedIdentity);

        // Ensure expected role assignment exists for explicitly created EMSI
        //
        PagedIterable<RoleAssignment> roleAssignmentsForNetwork =
            this.msiManager.authorizationManager().roleAssignments().listByScope(network.id());
        boolean found = false;
        for (RoleAssignment roleAssignment : roleAssignmentsForNetwork) {
            if (roleAssignment.principalId() != null
                && roleAssignment.principalId().equalsIgnoreCase(createdIdentity.principalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(
                found,
                "Expected role assignment not found for the virtual network for identity" + createdIdentity.name());

        RoleAssignment assignment =
            lookupRoleAssignmentUsingScopeAndRoleAsync(network.id(), BuiltInRole.READER, createdIdentity.principalId())
                .block();

        Assertions
            .assertNotNull(
                assignment, "Expected role assignment with ROLE not found for the virtual network for identity");

        // Ensure expected role assignment exists for explicitly created EMSI
        //
        PagedIterable<RoleAssignment> roleAssignmentsForResourceGroup =
            this.msiManager.authorizationManager().roleAssignments().listByScope(resourceGroup.id());

        found = false;
        for (RoleAssignment roleAssignment : roleAssignmentsForResourceGroup) {
            if (roleAssignment.principalId() != null
                && roleAssignment.principalId().equalsIgnoreCase(implicitlyCreatedIdentity.principalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(
                found,
                "Expected role assignment not found for the resource group for identity"
                    + implicitlyCreatedIdentity.name());

        assignment =
            lookupRoleAssignmentUsingScopeAndRoleAsync(
                    resourceGroup.id(), BuiltInRole.CONTRIBUTOR, implicitlyCreatedIdentity.principalId())
                .block();

        Assertions
            .assertNotNull(
                assignment, "Expected role assignment with ROLE not found for the resource group for identity");

        emsiIds = virtualMachineScaleSet.userAssignedManagedServiceIdentityIds();
        Iterator<String> itr = emsiIds.iterator();
        // Remove both (all) identities
        virtualMachineScaleSet
            .update()
            .withoutUserAssignedManagedServiceIdentity(itr.next())
            .withoutUserAssignedManagedServiceIdentity(itr.next())
            .apply();

        //
        Assertions.assertEquals(0, virtualMachineScaleSet.userAssignedManagedServiceIdentityIds().size());
        if (virtualMachineScaleSet.managedServiceIdentityType() != null) {
            Assertions
                .assertTrue(virtualMachineScaleSet.managedServiceIdentityType().equals(ResourceIdentityType.NONE));
        }
        // fetch vm again and validate
        virtualMachineScaleSet.refresh();
        //
        Assertions.assertEquals(0, virtualMachineScaleSet.userAssignedManagedServiceIdentityIds().size());
        if (virtualMachineScaleSet.managedServiceIdentityType() != null) {
            Assertions
                .assertTrue(virtualMachineScaleSet.managedServiceIdentityType().equals(ResourceIdentityType.NONE));
        }
        //
        //
        itr = emsiIds.iterator();
        Identity identity1 = msiManager.identities().getById(itr.next());
        Identity identity2 = msiManager.identities().getById(itr.next());
        //
        // Update VM by enabling System-MSI and add two identities
        virtualMachineScaleSet
            .update()
            .withSystemAssignedManagedServiceIdentity()
            .withExistingUserAssignedManagedServiceIdentity(identity1)
            .withExistingUserAssignedManagedServiceIdentity(identity2)
            .apply();

        Assertions.assertNotNull(virtualMachineScaleSet.userAssignedManagedServiceIdentityIds());
        Assertions.assertEquals(2, virtualMachineScaleSet.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachineScaleSet.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachineScaleSet
                    .managedServiceIdentityType()
                    .equals(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED));
        //
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityTenantId());
        //
        virtualMachineScaleSet.refresh();
        Assertions.assertNotNull(virtualMachineScaleSet.userAssignedManagedServiceIdentityIds());
        Assertions.assertEquals(2, virtualMachineScaleSet.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachineScaleSet.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachineScaleSet
                    .managedServiceIdentityType()
                    .equals(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED));
        //
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityTenantId());
        //
        itr = emsiIds.iterator();
        // Remove identities one by one (first one)
        virtualMachineScaleSet.update().withoutUserAssignedManagedServiceIdentity(itr.next()).apply();
        //
        Assertions.assertNotNull(virtualMachineScaleSet.userAssignedManagedServiceIdentityIds());
        Assertions.assertEquals(1, virtualMachineScaleSet.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachineScaleSet.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachineScaleSet
                    .managedServiceIdentityType()
                    .equals(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED));
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityTenantId());
        // Remove identities one by one (second one)
        virtualMachineScaleSet.update().withoutUserAssignedManagedServiceIdentity(itr.next()).apply();
        //
        Assertions.assertEquals(0, virtualMachineScaleSet.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachineScaleSet.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachineScaleSet.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED));
        //
        virtualMachineScaleSet.update().withoutSystemAssignedManagedServiceIdentity().apply();
        Assertions.assertEquals(0, virtualMachineScaleSet.userAssignedManagedServiceIdentityIds().size());
        if (virtualMachineScaleSet.managedServiceIdentityType() != null) {
            Assertions
                .assertTrue(virtualMachineScaleSet.managedServiceIdentityType().equals(ResourceIdentityType.NONE));
        }
        Assertions.assertNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityTenantId());
    }

    @Test
    public void canCreateVirtualMachineScaleSetWithLMSIAndEMSI() throws Exception {
        rgName = generateRandomResourceName("java-emsi-c-rg", 15);
        String identityName1 = generateRandomResourceName("msi-id", 15);
        String networkName = generateRandomResourceName("nw", 10);

        // Create a resource group
        //
        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Create a virtual network to which we will assign "EMSI" with reader access
        //
        Network network =
            networkManager
                .networks()
                .define(networkName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .create();

        // Prepare a definition for yet-to-be-created "User Assigned (External) MSI" with contributor access to the
        // resource group
        // it resides
        //
        Creatable<Identity> creatableIdentity =
            msiManager
                .identities()
                .define(identityName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);

        // Create a virtual network for VMSS
        //
        Network vmssNetwork =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        // Create a Load balancer for VMSS
        //
        LoadBalancer vmssInternalLoadBalancer = createInternalLoadBalancer(region, resourceGroup, vmssNetwork, "1");

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(vmssNetwork, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withExistingPrimaryInternalLoadBalancer(vmssInternalLoadBalancer)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessTo(network.id(), BuiltInRole.CONTRIBUTOR)
                .withNewUserAssignedManagedServiceIdentity(creatableIdentity)
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertNotNull(virtualMachineScaleSet.innerModel());
        Assertions.assertTrue(virtualMachineScaleSet.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityTenantId());

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
        //
        Set<String> emsiIds = virtualMachineScaleSet.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(1, emsiIds.size());

        Identity identity = msiManager.identities().getById(emsiIds.iterator().next());
        Assertions.assertNotNull(identity);
        Assertions.assertTrue(identity.name().equalsIgnoreCase(identityName1));

        // Ensure expected role assignment exists for LMSI
        //
        PagedIterable<RoleAssignment> roleAssignmentsForNetwork =
            this.msiManager.authorizationManager().roleAssignments().listByScope(network.id());
        boolean found = false;
        for (RoleAssignment roleAssignment : roleAssignmentsForNetwork) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(
                found,
                "Expected role assignment not found for the virtual network for local identity"
                    + virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());

        RoleAssignment assignment =
            lookupRoleAssignmentUsingScopeAndRoleAsync(
                    network.id(),
                    BuiltInRole.CONTRIBUTOR,
                    virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId())
                .block();

        Assertions
            .assertNotNull(
                assignment,
                "Expected role assignment with ROLE not found for the virtual network for system assigned identity");

        // Ensure expected role assignment exists for EMSI
        //
        PagedIterable<RoleAssignment> roleAssignmentsForResourceGroup =
            this
                .msiManager
                .authorizationManager()
                .roleAssignments()
                .listByScope(
                    resourceManager.resourceGroups().getByName(virtualMachineScaleSet.resourceGroupName()).id());
        found = false;
        for (RoleAssignment roleAssignment : roleAssignmentsForResourceGroup) {
            if (roleAssignment.principalId() != null
                && roleAssignment.principalId().equalsIgnoreCase(identity.principalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(
                found, "Expected role assignment not found for the resource group for identity" + identity.name());

        assignment =
            lookupRoleAssignmentUsingScopeAndRoleAsync(
                    resourceGroup.id(), BuiltInRole.CONTRIBUTOR, identity.principalId())
                .block();

        Assertions
            .assertNotNull(
                assignment,
                "Expected role assignment with ROLE not found for the resource group for system assigned identity");
    }

    @Test
    public void canUpdateVirtualMachineScaleSetWithEMSIAndLMSI() throws Exception {
        rgName = generateRandomResourceName("java-emsi-c-rg", 15);
        String identityName1 = generateRandomResourceName("msi-id-1", 15);
        String identityName2 = generateRandomResourceName("msi-id-2", 15);

        // Create a resource group
        //
        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Create a virtual network for VMSS
        //
        Network vmssNetwork =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        // Create a Load balancer for VMSS
        //
        LoadBalancer vmssInternalLoadBalancer = createInternalLoadBalancer(region, resourceGroup, vmssNetwork, "1");

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(vmssNetwork, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withExistingPrimaryInternalLoadBalancer(vmssInternalLoadBalancer)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .create();

        // Prepare a definition for yet-to-be-created "User Assigned (External) MSI" with contributor access to the
        // resource group
        // it resides
        //
        Creatable<Identity> creatableIdentity =
            msiManager
                .identities()
                .define(identityName1)
                .withRegion(region)
                .withExistingResourceGroup(virtualMachineScaleSet.resourceGroupName())
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);

        // Update virtual machine so that it depends on the EMSI
        //
        virtualMachineScaleSet =
            virtualMachineScaleSet.update().withNewUserAssignedManagedServiceIdentity(creatableIdentity).apply();

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
        //
        Set<String> emsiIds = virtualMachineScaleSet.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(1, emsiIds.size());

        Identity identity = msiManager.identities().getById(emsiIds.iterator().next());
        Assertions.assertNotNull(identity);
        Assertions.assertTrue(identity.name().equalsIgnoreCase(identityName1));

        // Update VMSS without modify MSI
        virtualMachineScaleSet.update()
            .withNewDataDisk(10)
            .apply();
        emsiIds = virtualMachineScaleSet.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(1, emsiIds.size());

        // Creates an EMSI
        //
        Identity createdIdentity =
            msiManager
                .identities()
                .define(identityName2)
                .withRegion(region)
                .withExistingResourceGroup(virtualMachineScaleSet.resourceGroupName())
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .create();

        // Update the virtual machine by removing the an EMSI and adding existing EMSI
        //
        virtualMachineScaleSet =
            virtualMachineScaleSet
                .update()
                .withoutUserAssignedManagedServiceIdentity(identity.id())
                .withExistingUserAssignedManagedServiceIdentity(createdIdentity)
                .apply();

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
        //
        emsiIds = virtualMachineScaleSet.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(1, emsiIds.size());

        identity = msiManager.identities().getById(emsiIds.iterator().next());
        Assertions.assertNotNull(identity);
        Assertions.assertTrue(identity.name().equalsIgnoreCase(identityName2));

        // Update the virtual machine by enabling "LMSI"

        virtualMachineScaleSet.update().withSystemAssignedManagedServiceIdentity().apply();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertNotNull(virtualMachineScaleSet.innerModel());
        Assertions.assertTrue(virtualMachineScaleSet.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachineScaleSet.systemAssignedManagedServiceIdentityTenantId());
    }

    private Mono<RoleAssignment> lookupRoleAssignmentUsingScopeAndRoleAsync(
        final String scope, BuiltInRole role, final String principalId) {
        return this
            .msiManager
            .authorizationManager()
            .roleDefinitions()
            .getByScopeAndRoleNameAsync(scope, role.toString())
            .flatMap(
                roleDefinition ->
                    msiManager
                        .authorizationManager()
                        .roleAssignments()
                        .listByScopeAsync(scope)
                        .filter(
                            roleAssignment ->
                                roleAssignment.roleDefinitionId().equalsIgnoreCase(roleDefinition.id())
                                    && roleAssignment.principalId().equalsIgnoreCase(principalId))
                        .singleOrEmpty())
            .switchIfEmpty(Mono.defer(() -> Mono.empty()));
    }
}
