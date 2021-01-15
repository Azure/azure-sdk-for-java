// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceGroupsTests extends ResourceManagementTest {
    private ResourceGroups resourceGroups;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        super.initializeClients(httpPipeline, profile);
        resourceGroups = resourceClient.resourceGroups();
    }

    @Test
    public void canCreateResourceGroup() throws Exception {
        final String rgName = generateRandomResourceName("rg", 9);
        Region region = Region.US_SOUTH_CENTRAL;
        // Create
        resourceGroups.define(rgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withTag("department", "finance")
                .withTag("tagname", "tagvalue")
                .create();
        // List
        ResourceGroup groupResult = null;
        for (ResourceGroup rg : resourceGroups.listByTag("department", "finance")) {
            if (rg.name().equals(rgName)) {
                groupResult = rg;
                break;
            }
        }
        Assertions.assertNotNull(groupResult);
        Assertions.assertEquals("finance", groupResult.tags().get("department"));
        Assertions.assertEquals("tagvalue", groupResult.tags().get("tagname"));
        Assertions.assertTrue(region.name().equalsIgnoreCase(groupResult.regionName()));

        // Check existence
        Assertions.assertTrue(resourceGroups.contain(rgName));

        // Get
        ResourceGroup getGroup = resourceGroups.getByName(rgName);
        Assertions.assertNotNull(getGroup);
        Assertions.assertEquals(rgName, getGroup.name());
        // Update
        ResourceGroup updatedGroup = getGroup.update()
                .withTag("tag1", "value1")
                .apply();
        Assertions.assertEquals("value1", updatedGroup.tags().get("tag1"));
        Assertions.assertTrue(region.name().equalsIgnoreCase(getGroup.regionName()));
        // Delete
        resourceGroups.beginDeleteByName(rgName);
        // Assertions.assertFalse(resourceGroups.checkExistence(rgName));
    }
}
