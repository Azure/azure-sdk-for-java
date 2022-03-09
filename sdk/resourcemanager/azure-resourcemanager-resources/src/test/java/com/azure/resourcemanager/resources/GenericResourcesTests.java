// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.resourcemanager.resources.fluent.models.GenericResourceExpandedInner;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.GenericResources;
import com.azure.resourcemanager.resources.models.Identity;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.models.ResourceIdentityType;
import com.azure.resourcemanager.resources.models.Sku;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GenericResourcesTests extends ResourceManagementTest {
    private ResourceGroups resourceGroups;
    private GenericResources genericResources;

    private String testId;
    private String rgName;
    private String newRgName;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        testId = generateRandomResourceName("", 9);
        rgName = "rg" + testId;
        newRgName = "rgB" + testId;

        super.initializeClients(httpPipeline, profile);
        resourceGroups = resourceClient.resourceGroups();
        genericResources = resourceClient.genericResources();
        resourceGroups.define(rgName)
                .withRegion(Region.US_EAST)
                .create();
        resourceGroups.define(newRgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .create();
    }

    @Override
    protected void cleanUpResources() {
        resourceGroups.beginDeleteByName(newRgName);
        resourceGroups.beginDeleteByName(rgName);
    }

    @Test
    public void canCreateUpdateMoveResource() throws Exception {
        final String resourceName = "rs" + testId;
        // Create
        GenericResource resource = genericResources.define(resourceName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withResourceType("sites")
                .withProviderNamespace("Microsoft.Web")
                .withoutPlan()
                .withParentResourcePath("")
                .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Shared\"}"))
                .create();
        //List
        PagedIterable<GenericResource> resourceList = genericResources.listByResourceGroup(rgName);
        boolean found = false;
        for (GenericResource gr : resourceList) {
            if (gr.name().equals(resource.name())) {
                Assertions.assertNotNull(gr.apiVersion());
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // Get
        Assertions.assertNotNull(genericResources.get(rgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion()));
        // Move
        genericResources.moveResources(rgName, resourceGroups.getByName(newRgName), Arrays.asList(resource.id()));
        Assertions.assertFalse(genericResources.checkExistence(rgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion()));
        resource = genericResources.get(newRgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion());
        Assertions.assertNotNull(resource);
        // Update
        resource.update()
                .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Dynamic\"}"))
                .apply();
        // Delete
        genericResources.deleteById(resource.id());
        Assertions.assertFalse(genericResources.checkExistence(newRgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion()));
        Assertions.assertFalse(genericResources.checkExistenceById(resource.id()));
    }

    @Test
    public void canValidateMoveResources() throws Exception {
        final String resourceName = "rs" + testId;
        final Map<String, Object> properties = new HashMap<>();
        properties.put("publicIPAllocationMethod", "Dynamic");

        GenericResource resource = genericResources.define(resourceName)
            .withRegion(Region.US_EAST2)
            .withExistingResourceGroup(rgName)
            .withResourceType("publicIPAddresses")
            .withProviderNamespace("Microsoft.Network")
            .withoutPlan()
            .withProperties(properties)
            .create();

        ResourceGroup targetResourceGroup = resourceGroups.getByName(newRgName);
        // validate pass as public IP can be moved
        genericResources.validateMoveResources(rgName, targetResourceGroup, Collections.singletonList(resource.id()));

        // create resource in target group with same name
        GenericResource resource2 = genericResources.define(resourceName)
            .withRegion(Region.US_EAST2)
            .withExistingResourceGroup(newRgName)
            .withResourceType("publicIPAddresses")
            .withProviderNamespace("Microsoft.Network")
            .withoutPlan()
            .withProperties(properties)
            .create();

        // validate fail as name conflict
        Assertions.assertThrows(ManagementException.class, () -> {
            genericResources.validateMoveResources(rgName, targetResourceGroup, Collections.singletonList(resource.id()));
        });

        final String resourceName3 = "rs2" + testId;
        GenericResource resource3 = genericResources.define(resourceName3)
            .withRegion(Region.US_EAST2)
            .withExistingResourceGroup(rgName)
            .withResourceType("userAssignedIdentities")
            .withProviderNamespace("Microsoft.ManagedIdentity")
            .withoutPlan()
            .create();

        // validate fail as managed identity does not support move
        Assertions.assertThrows(ManagementException.class, () -> {
            genericResources.validateMoveResources(rgName, targetResourceGroup, Collections.singletonList(resource3.id()));
        });
    }

    @Test
    public void canCreateDeleteResourceSyncPoll() throws Exception {
        final long defaultDelayInMillis = 10 * 1000;

        final String resourceName = "rs" + testId;
        // Create
        Accepted<GenericResource> acceptedResource = genericResources.define(resourceName)
            .withRegion(Region.US_SOUTH_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withResourceType("sites")
            .withProviderNamespace("Microsoft.Web")
            .withoutPlan()
            .withParentResourcePath("")
            .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Shared\"}"))
            .beginCreate();

        LongRunningOperationStatus pollStatus = acceptedResource.getActivationResponse().getStatus();
        long delayInMills = acceptedResource.getActivationResponse().getRetryAfter() == null
            ? defaultDelayInMillis
            : acceptedResource.getActivationResponse().getRetryAfter().toMillis();
        while (!pollStatus.isComplete()) {
            ResourceManagerUtils.sleep(Duration.ofMillis(delayInMills));

            PollResponse<?> pollResponse = acceptedResource.getSyncPoller().poll();
            pollStatus = pollResponse.getStatus();
            delayInMills = pollResponse.getRetryAfter() == null
                ? defaultDelayInMillis
                : pollResponse.getRetryAfter().toMillis();
        }
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollStatus);
        GenericResource resource = acceptedResource.getFinalResult();
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(resourceName, ResourceUtils.nameFromResourceId(resource.id()));

        PagedIterable<GenericResourceExpandedInner> resources =
            genericResources.manager().serviceClient().getResources()
                .listByResourceGroup(rgName, null, "provisioningState", null, Context.NONE);
        Optional<GenericResourceExpandedInner> resourceOpt
            = resources.stream().filter(r -> resourceName.equals(r.name())).findFirst();
        Assertions.assertTrue(resourceOpt.isPresent());
        Assertions.assertEquals("Succeeded", resourceOpt.get().provisioningState());

        Accepted<Void> acceptedDelete = genericResources.beginDeleteById(resource.id());
        acceptedDelete.getFinalResult();
        PagedIterable<GenericResource> resourcesAfterDelete = genericResources.listByResourceGroup(rgName);
        boolean deleted = resourcesAfterDelete.stream().noneMatch(r -> resourceName.equals(r.name()));
        Assertions.assertTrue(deleted);
    }

    @Test
    public void canCreateUpdateKindSkuIdentity() throws Exception {
        final String resourceName = "rs" + testId;
        final String apiVersion = "2021-01-01";

        GenericResource storageResource = resourceClient.genericResources().define(resourceName)
            .withRegion(Region.US_WEST)
            .withExistingResourceGroup(rgName)
            .withResourceType("storageAccounts")
            .withProviderNamespace("Microsoft.Storage")
            .withoutPlan()
            .withKind("Storage")
            .withSku(new Sku().withName("Standard_LRS"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(new ObjectMapper().readTree("{\"minimumTlsVersion\": \"TLS1_2\", \"supportsHttpsTrafficOnly\": true}"))
            .withApiVersion(apiVersion)
            .create();
        Assertions.assertEquals("Storage", storageResource.kind());
        Assertions.assertEquals("Standard_LRS", storageResource.sku().name());
        Assertions.assertNotNull(storageResource.identity());
        Assertions.assertEquals(ResourceIdentityType.SYSTEM_ASSIGNED, storageResource.identity().type());
        Assertions.assertNotNull(storageResource.identity().principalId());
        Assertions.assertNotNull(storageResource.identity().tenantId());

        storageResource.update()
            .withKind("StorageV2")
            .withoutIdentity()
            .withApiVersion(apiVersion)
            .apply();
        Assertions.assertEquals("StorageV2", storageResource.kind());
        Assertions.assertEquals(ResourceIdentityType.NONE, storageResource.identity().type());

        storageResource.update()
            .withSku(new Sku().withName("Standard_RAGRS"))
            .withApiVersion(apiVersion)
            .apply();
        Assertions.assertEquals("Standard_RAGRS", storageResource.sku().name());
    }
}
