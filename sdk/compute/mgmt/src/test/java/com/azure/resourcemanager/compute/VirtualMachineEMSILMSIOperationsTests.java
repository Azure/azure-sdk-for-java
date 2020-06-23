// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.msi.MSIManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class VirtualMachineEMSILMSIOperationsTests extends TestBase {
    private String rgName = "";
    private Region region = Region.fromName("West Central US");
    private final String vmName = "javavm";

    private ComputeManager computeManager;
    private MSIManager msiManager;
    private ResourceManager resourceManager;
    private NetworkManager networkManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile)
        throws IOException {
        this.msiManager = MSIManager.authenticate(httpPipeline, profile, sdkContext);
        this.resourceManager = msiManager.resourceManager();
        this.computeManager = ComputeManager.authenticate(httpPipeline, profile, sdkContext);
        this.networkManager = NetworkManager.authenticate(httpPipeline, profile, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
        this.resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateUpdateVirtualMachineWithEMSI() {
        // this.resourceManager.resourceGroups().beginDeleteByName("41522c6e938c4f6");

        rgName = generateRandomResourceName("java-emsi-c-rg", 15);
        String identityName1 = generateRandomResourceName("msi-id", 15);
        String identityName2 = generateRandomResourceName("msi-id", 15);
        String networkName = generateRandomResourceName("nw", 10);

        // Prepare a definition for yet-to-be-created resource group
        //
        Creatable<ResourceGroup> creatableRG = resourceManager.resourceGroups().define(rgName).withRegion(region);

        // Create a virtual network residing in the above RG
        //
        final Network network =
            networkManager.networks().define(networkName).withRegion(region).withNewResourceGroup(creatableRG).create();

        // Create an "User Assigned (External) MSI" residing in the above RG and assign reader access to the virtual
        // network
        //
        final Identity createdIdentity =
            msiManager
                .identities()
                .define(identityName1)
                .withRegion(region)
                .withNewResourceGroup(creatableRG)
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
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);

        // Create a virtual machine and associate it with existing and yet-t-be-created identities
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
                .withRootPassword(password())
                .withExistingUserAssignedManagedServiceIdentity(createdIdentity)
                .withNewUserAssignedManagedServiceIdentity(creatableIdentity)
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNotNull(virtualMachine.inner());
        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId()); // No Local MSI enabled
        Assertions.assertNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId()); // No Local MSI enabled

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
        //
        Set<String> emsiIds = virtualMachine.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(2, emsiIds.size());

        // Ensure the "User Assigned (External) MSI"s matches with the those provided as part of VM create
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
            this.msiManager.graphRbacManager().roleAssignments().listByScope(network.id());
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
        ResourceGroup resourceGroup = resourceManager.resourceGroups().getByName(virtualMachine.resourceGroupName());
        Assertions.assertNotNull(resourceGroup);

        PagedIterable<RoleAssignment> roleAssignmentsForResourceGroup =
            this.msiManager.graphRbacManager().roleAssignments().listByScope(resourceGroup.id());
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

        emsiIds = virtualMachine.userAssignedManagedServiceIdentityIds();
        Iterator<String> itr = emsiIds.iterator();
        // Remove both (all) identities
        virtualMachine
            .update()
            .withoutUserAssignedManagedServiceIdentity(itr.next())
            .withoutUserAssignedManagedServiceIdentity(itr.next())
            .apply();
        //
        Assertions.assertEquals(0, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        if (virtualMachine.managedServiceIdentityType() != null) {
            Assertions.assertTrue(virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.NONE));
        }
        // fetch vm again and validate
        virtualMachine.refresh();
        //
        Assertions.assertEquals(0, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        if (virtualMachine.managedServiceIdentityType() != null) {
            Assertions.assertTrue(virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.NONE));
        }
        //
        itr = emsiIds.iterator();
        Identity identity1 = msiManager.identities().getById(itr.next());
        Identity identity2 = msiManager.identities().getById(itr.next());
        //
        // Update VM by enabling System-MSI and add two identities
        virtualMachine
            .update()
            .withSystemAssignedManagedServiceIdentity()
            .withExistingUserAssignedManagedServiceIdentity(identity1)
            .withExistingUserAssignedManagedServiceIdentity(identity2)
            .apply();

        Assertions.assertNotNull(virtualMachine.userAssignedManagedServiceIdentityIds());
        Assertions.assertEquals(2, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachine.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED__USER_ASSIGNED));
        //
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());
        //
        virtualMachine.refresh();
        Assertions.assertNotNull(virtualMachine.userAssignedManagedServiceIdentityIds());
        Assertions.assertEquals(2, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachine.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED__USER_ASSIGNED));
        //
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());
        //
        itr = emsiIds.iterator();
        // Remove identities one by one (first one)
        virtualMachine.update().withoutUserAssignedManagedServiceIdentity(itr.next()).apply();
        //
        Assertions.assertNotNull(virtualMachine.userAssignedManagedServiceIdentityIds());
        Assertions.assertEquals(1, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachine.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED__USER_ASSIGNED));
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());
        // Remove identities one by one (second one)
        virtualMachine.update().withoutUserAssignedManagedServiceIdentity(itr.next()).apply();
        //
        Assertions.assertEquals(0, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        Assertions.assertNotNull(virtualMachine.managedServiceIdentityType());
        Assertions.assertTrue(virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED));
        //
    }

    @Test
    public void canCreateVirtualMachineWithLMSIAndEMSI() {
        rgName = generateRandomResourceName("java-emsi-c-rg", 15);
        String identityName1 = generateRandomResourceName("msi-id", 15);
        String networkName = generateRandomResourceName("nw", 10);

        // Create a resource group
        //
        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Create a virtual network
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

        // Create a virtual machine and associate it with existing and yet-to-be-created identities
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
                .withRootPassword(password())
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessTo(network.id(), BuiltInRole.CONTRIBUTOR)
                .withNewUserAssignedManagedServiceIdentity(creatableIdentity)
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNotNull(virtualMachine.inner());
        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
        //
        Set<String> emsiIds = virtualMachine.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(1, emsiIds.size());

        Identity identity = msiManager.identities().getById(emsiIds.iterator().next());
        Assertions.assertNotNull(identity);
        Assertions.assertTrue(identity.name().equalsIgnoreCase(identityName1));

        // Ensure expected role assignment exists for LMSI
        //
        PagedIterable<RoleAssignment> roleAssignmentsForNetwork =
            this.msiManager.graphRbacManager().roleAssignments().listByScope(network.id());

        boolean found = false;
        for (RoleAssignment roleAssignment : roleAssignmentsForNetwork) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                    .principalId()
                    .equalsIgnoreCase(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(
                found,
                "Expected role assignment not found for the virtual network for local identity"
                    + virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());

        RoleAssignment assignment =
            lookupRoleAssignmentUsingScopeAndRoleAsync(
                    network.id(),
                    BuiltInRole.CONTRIBUTOR,
                    virtualMachine.systemAssignedManagedServiceIdentityPrincipalId())
                .block();

        Assertions
            .assertNotNull(
                assignment,
                "Expected role assignment with ROLE not found for the virtual network for system assigned identity");

        // Ensure expected role assignment exists for EMSI
        //
        ResourceGroup resourceGroup1 = resourceManager.resourceGroups().getByName(virtualMachine.resourceGroupName());

        PagedIterable<RoleAssignment> roleAssignmentsForResourceGroup =
            this.msiManager.graphRbacManager().roleAssignments().listByScope(resourceGroup1.id());
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
                    resourceGroup1.id(), BuiltInRole.CONTRIBUTOR, identity.principalId())
                .block();

        Assertions
            .assertNotNull(
                assignment,
                "Expected role assignment with ROLE not found for the resource group for system assigned identity");
    }

    @Test
    public void canUpdateVirtualMachineWithEMSIAndLMSI() throws Exception {
        rgName = generateRandomResourceName("java-emsi-c-rg", 15);
        String identityName1 = generateRandomResourceName("msi-id-1", 15);
        String identityName2 = generateRandomResourceName("msi-id-2", 15);

        // Create a virtual machine with no EMSI & LMSI
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
                .withRootPassword(password())
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
                .withExistingResourceGroup(virtualMachine.resourceGroupName())
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);

        // Update virtual machine so that it depends on the EMSI
        //
        virtualMachine = virtualMachine.update().withNewUserAssignedManagedServiceIdentity(creatableIdentity).apply();

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
        //
        Set<String> emsiIds = virtualMachine.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(1, emsiIds.size());

        Identity identity = msiManager.identities().getById(emsiIds.iterator().next());
        Assertions.assertNotNull(identity);
        Assertions.assertTrue(identity.name().equalsIgnoreCase(identityName1));

        // Creates an EMSI
        //
        Identity createdIdentity =
            msiManager
                .identities()
                .define(identityName2)
                .withRegion(region)
                .withExistingResourceGroup(virtualMachine.resourceGroupName())
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .create();

        // Update the virtual machine by removing the an EMSI and adding existing EMSI
        //
        virtualMachine =
            virtualMachine
                .update()
                .withoutUserAssignedManagedServiceIdentity(identity.id())
                .withExistingUserAssignedManagedServiceIdentity(createdIdentity)
                .apply();

        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
        //
        emsiIds = virtualMachine.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(emsiIds);
        Assertions.assertEquals(1, emsiIds.size());

        identity = msiManager.identities().getById(emsiIds.iterator().next());
        Assertions.assertNotNull(identity);
        Assertions.assertTrue(identity.name().equalsIgnoreCase(identityName2));

        // Update the virtual machine by enabling "LMSI"

        virtualMachine.update().withSystemAssignedManagedServiceIdentity().apply();
        //
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNotNull(virtualMachine.inner());
        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachine.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED__USER_ASSIGNED));
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertEquals(1, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        //
        virtualMachine.update().withoutSystemAssignedManagedServiceIdentity().apply();

        Assertions.assertTrue(virtualMachine.isManagedServiceIdentityEnabled());
        Assertions.assertNotNull(virtualMachine.managedServiceIdentityType());
        Assertions.assertTrue(virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.USER_ASSIGNED));
        Assertions.assertNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertEquals(1, virtualMachine.userAssignedManagedServiceIdentityIds().size());
        //
        virtualMachine.update().withoutUserAssignedManagedServiceIdentity(identity.id()).apply();
        Assertions.assertFalse(virtualMachine.isManagedServiceIdentityEnabled());
        if (virtualMachine.managedServiceIdentityType() != null) {
            Assertions.assertTrue(virtualMachine.managedServiceIdentityType().equals(ResourceIdentityType.NONE));
        }
        Assertions.assertNull(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNull(virtualMachine.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertEquals(0, virtualMachine.userAssignedManagedServiceIdentityIds().size());
    }

    private Mono<RoleAssignment> lookupRoleAssignmentUsingScopeAndRoleAsync(
        final String scope, BuiltInRole role, final String principalId) {
        return this
            .msiManager
            .graphRbacManager()
            .roleDefinitions()
            .getByScopeAndRoleNameAsync(scope, role.toString())
            .flatMap(
                roleDefinition ->
                    msiManager
                        .graphRbacManager()
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
