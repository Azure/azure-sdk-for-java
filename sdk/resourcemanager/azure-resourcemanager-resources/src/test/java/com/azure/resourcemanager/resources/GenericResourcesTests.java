// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.resourcemanager.resources.fluent.inner.GenericResourceExpandedInner;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.GenericResources;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

public class GenericResourcesTests extends ResourceManagerTestBase {
    private ResourceGroups resourceGroups;
    private GenericResources genericResources;

    private String testId;
    private String rgName;
    private String newRgName;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        testId = sdkContext.randomResourceName("", 9);
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
    public void canCreateDeleteResourceSyncPoll() throws Exception {
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
        int delayInMills = acceptedResource.getActivationResponse().getRetryAfter() == null
            ? 0
            : (int) acceptedResource.getActivationResponse().getRetryAfter().toMillis();
        while (!pollStatus.isComplete()) {
            SdkContext.sleep(delayInMills);

            PollResponse<?> pollResponse = acceptedResource.getSyncPoller().poll();
            pollStatus = pollResponse.getStatus();
            delayInMills = pollResponse.getRetryAfter() == null
                ? 10000
                : (int) pollResponse.getRetryAfter().toMillis();
        }
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollStatus);
        GenericResource resource = acceptedResource.getFinalResult();
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(resourceName, ResourceUtils.nameFromResourceId(resource.id()));

        PagedIterable<GenericResourceExpandedInner> resources =
            genericResources.manager().inner().getResources()
                .listByResourceGroup(rgName, null, "provisioningState", null);
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
}
