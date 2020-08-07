// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.msi;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MSIIdentityManagementTests extends TestBase {
    private String rgName = "";
    private Region region = Region.fromName("West Central US");

    private MSIManager msiManager;
    private ResourceManager resourceManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) throws IOException {
        this.msiManager = MSIManager.authenticate(httpPipeline, profile, sdkContext);
        this.resourceManager = msiManager.resourceManager();
    }

    @Override
    protected void cleanUpResources() {
        this.resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateGetListDeleteIdentity() throws Exception {
        rgName = generateRandomResourceName("javaismrg", 15);
        String identityName = generateRandomResourceName("msi-id", 15);

        Creatable<ResourceGroup> creatableRG = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        Identity identity = msiManager.identities()
                .define(identityName)
                .withRegion(region)
                .withNewResourceGroup(creatableRG)
                .create();

        Assertions.assertNotNull(identity);
        Assertions.assertNotNull(identity.inner());
        Assertions.assertTrue(identityName.equalsIgnoreCase(identity.name()), String.format("%s == %s", identityName, identity.name()));
        Assertions.assertTrue(rgName.equalsIgnoreCase(identity.resourceGroupName()), String.format("%s == %s", rgName, identity.resourceGroupName()));

        Assertions.assertNotNull(identity.clientId());
        Assertions.assertNotNull(identity.principalId());
        Assertions.assertNotNull(identity.tenantId());
        //Assertions.assertNotNull(identity.clientSecretUrl());

        identity = msiManager.identities().getById(identity.id());

        Assertions.assertNotNull(identity);
        Assertions.assertNotNull(identity.inner());

        PagedIterable<Identity> identities = msiManager.identities()
                .listByResourceGroup(rgName);

        Assertions.assertNotNull(identities);

        boolean found = false;
        for (Identity id : identities) {
            Assertions.assertNotNull(id);
            Assertions.assertNotNull(id.inner());
            if (id.name().equalsIgnoreCase(identityName)) {
                found = true;
            }
            Assertions.assertNotNull(identity.clientId());
            Assertions.assertNotNull(identity.principalId());
            Assertions.assertNotNull(identity.tenantId());
            //Assertions.assertNotNull(identity.clientSecretUrl());
        }

        Assertions.assertTrue(found);

        msiManager.identities()
                .deleteById(identity.id());
    }

    @Test
    public void canAssignCurrentResourceGroupAccessRoleToIdentity() throws Exception {
        rgName = generateRandomResourceName("javaismrg", 15);
        String identityName = generateRandomResourceName("msi-id", 15);

        Creatable<ResourceGroup> creatableRG = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        Identity identity = msiManager.identities()
                .define(identityName)
                .withRegion(region)
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.READER)
                .create();

        // Ensure role assigned
        //
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().getByName(identity.resourceGroupName());
        PagedIterable<RoleAssignment> roleAssignments = this.msiManager.graphRbacManager().roleAssignments().listByScope(resourceGroup.id());
        boolean found = false;
        for (RoleAssignment roleAssignment : roleAssignments) {
            if (roleAssignment.principalId() != null && roleAssignment.principalId().equalsIgnoreCase(identity.principalId())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Expected role assignment not found for the resource group that identity belongs to");

        identity.update()
                .withoutAccessTo(resourceGroup.id(), BuiltInRole.READER)
                .apply();

        SdkContext.sleep(30 * 1000);

        // Ensure role assignment removed
        //
        roleAssignments = this.msiManager.graphRbacManager().roleAssignments().listByScope(resourceGroup.id());
        boolean notFound = true;
        for (RoleAssignment roleAssignment : roleAssignments) {
            if (roleAssignment.principalId() != null && roleAssignment.principalId().equalsIgnoreCase(identity.principalId())) {
                notFound = false;
                break;
            }
        }
        Assertions.assertTrue(notFound, "Role assignment to access resource group is not removed");

        msiManager.identities()
                .deleteById(identity.id());

    }

    @Test
    public void canAssignRolesToIdentity() throws Exception {
        rgName = generateRandomResourceName("javaismrg", 15);
        String identityName = generateRandomResourceName("msi-id", 15);

        String anotherRgName = generateRandomResourceName("rg", 15);

        ResourceGroup anotherResourceGroup = resourceManager.resourceGroups()
                .define(anotherRgName)
                .withRegion(region)
                .create();

        Creatable<ResourceGroup> creatableRG = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        final List<Indexable> createdResosurces = new ArrayList<Indexable>();

        msiManager.identities()
                .define(identityName)
                .withRegion(region)
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.READER)
                .withAccessTo(anotherResourceGroup, BuiltInRole.CONTRIBUTOR)
                .createAsync()
                .doOnNext(indexable -> createdResosurces.add(indexable))
                .blockLast();

        int roleAssignmentResourcesCount = 0;
        int identityResourcesCount = 0;
        int resourceGroupResourcesCount = 0;
        Identity identity = null;

        for (Indexable resource : createdResosurces) {
            if (resource instanceof ResourceGroup) {
                resourceGroupResourcesCount++;
            } else if (resource instanceof RoleAssignment) {
                roleAssignmentResourcesCount++;
            } else if (resource instanceof Identity) {
                identityResourcesCount++;
                identity = (Identity) resource;
            }
        }

        Assertions.assertEquals(1, resourceGroupResourcesCount);
        Assertions.assertEquals(2, roleAssignmentResourcesCount);
        Assertions.assertEquals(2, identityResourcesCount); // Identity resource will be emitted twice - before & after post-run, will be fixed in graph
        Assertions.assertNotNull(identity);

        // Ensure roles are assigned
        //
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().getByName(identity.resourceGroupName());
        PagedIterable<RoleAssignment> roleAssignments = this.msiManager.graphRbacManager().roleAssignments().listByScope(resourceGroup.id());
        boolean found = false;
        for (RoleAssignment roleAssignment : roleAssignments) {
            if (roleAssignment.principalId() != null && roleAssignment.principalId().equalsIgnoreCase(identity.principalId())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Expected role assignment not found for the resource group that identity belongs to");

        roleAssignments = this.msiManager.graphRbacManager().roleAssignments().listByScope(anotherResourceGroup.id());
        found = false;
        for (RoleAssignment roleAssignment : roleAssignments) {
            if (roleAssignment.principalId() != null && roleAssignment.principalId().equalsIgnoreCase(identity.principalId())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Expected role assignment not found for the resource group resource");

        identity = identity
                .update()
                .withTag("a", "bb")
                .apply();

        Assertions.assertNotNull(identity.tags());
        Assertions.assertTrue(identity.tags().containsKey("a"));

        resourceManager.resourceGroups().deleteByName(anotherRgName);
    }
}
