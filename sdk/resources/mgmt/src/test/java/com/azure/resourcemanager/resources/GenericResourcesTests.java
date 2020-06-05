// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.GenericResources;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
}
