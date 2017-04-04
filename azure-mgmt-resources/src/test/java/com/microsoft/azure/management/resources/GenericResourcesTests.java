/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.RestClient;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class GenericResourcesTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;
    private static GenericResources genericResources;

    private String testId;
    private String rgName;
    private String newRgName;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        testId = SdkContext.randomResourceName("", 9);
        rgName = "rg" + testId;
        newRgName = "rgB" + testId;

        super.initializeClients(restClient, defaultSubscription, domain);
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
        resourceGroups.deleteByName(newRgName);
        resourceGroups.deleteByName(rgName);
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
        List<GenericResource> resourceList = genericResources.listByResourceGroup(rgName);
        boolean found = false;
        for (GenericResource gr: resourceList) {
            if (gr.name().equals(resource.name())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // Get
        Assert.assertNotNull(genericResources.get(rgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion()));
        // Move
        genericResources.moveResources(rgName, resourceGroups.getByName(newRgName), Arrays.asList(resource.id()));
        Assert.assertFalse(genericResources.checkExistence(rgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion()));
        resource = genericResources.get(newRgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion());
        Assert.assertNotNull(resource);
        // Update
        resource.update()
                .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Dynamic\"}"))
                .apply();
        // Delete
        genericResources.deleteById(resource.id());
        Assert.assertFalse(genericResources.checkExistence(newRgName, resource.resourceProviderNamespace(), resource.parentResourcePath(), resource.resourceType(), resource.name(), resource.apiVersion()));
        Assert.assertFalse(genericResources.checkExistenceById(resource.id()));
    }
}
