// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.msi;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MSIIdentityManagementTests extends ResourceManagerTestBase {
    private String rgName = "";
    private Region region = Region.fromName("West Central US");

    private MsiManager msiManager;
    private ResourceManager resourceManager;

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
        this.msiManager = buildManager(MsiManager.class, httpPipeline, profile);
        this.resourceManager = msiManager.resourceManager();
        setInternalContext(internalContext, msiManager);
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
        Assertions.assertNotNull(identity.innerModel());
        Assertions.assertTrue(identityName.equalsIgnoreCase(identity.name()), String.format("%s == %s", identityName, identity.name()));
        Assertions.assertTrue(rgName.equalsIgnoreCase(identity.resourceGroupName()), String.format("%s == %s", rgName, identity.resourceGroupName()));

        Assertions.assertNotNull(identity.clientId());
        Assertions.assertNotNull(identity.principalId());
        Assertions.assertNotNull(identity.tenantId());
        //Assertions.assertNotNull(identity.clientSecretUrl());

        identity = msiManager.identities().getById(identity.id());

        Assertions.assertNotNull(identity);
        Assertions.assertNotNull(identity.innerModel());

        PagedIterable<Identity> identities = msiManager.identities()
                .listByResourceGroup(rgName);

        Assertions.assertNotNull(identities);

        boolean found = false;
        for (Identity id : identities) {
            Assertions.assertNotNull(id);
            Assertions.assertNotNull(id.innerModel());
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
        PagedIterable<RoleAssignment> roleAssignments = this.msiManager.authorizationManager().roleAssignments().listByScope(resourceGroup.id());
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

        ResourceManagerUtils.sleep(Duration.ofSeconds(30));

        // Ensure role assignment removed
        //
        roleAssignments = this.msiManager.authorizationManager().roleAssignments().listByScope(resourceGroup.id());
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

        Identity identity = msiManager.identities()
                .define(identityName)
                .withRegion(region)
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.READER)
                .withAccessTo(anotherResourceGroup, BuiltInRole.CONTRIBUTOR)
                .createAsync()
                .block();

        Assertions.assertNotNull(identity);

        // Ensure roles are assigned
        //
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().getByName(identity.resourceGroupName());
        PagedIterable<RoleAssignment> roleAssignments = this.msiManager.authorizationManager().roleAssignments().listByScope(resourceGroup.id());
        boolean found = false;
        for (RoleAssignment roleAssignment : roleAssignments) {
            if (roleAssignment.principalId() != null && roleAssignment.principalId().equalsIgnoreCase(identity.principalId())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Expected role assignment not found for the resource group that identity belongs to");

        roleAssignments = this.msiManager.authorizationManager().roleAssignments().listByScope(anotherResourceGroup.id());
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
