/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class ResourceGroupsTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        super.initializeClients(restClient, defaultSubscription, domain);
        resourceGroups = resourceClient.resourceGroups();
    }

    @Test
    public void canCreateResourceGroup() throws Exception {
        final String rgName = SdkContext.randomResourceName("rg", 9);
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
        Assert.assertNotNull(groupResult);
        Assert.assertEquals("finance", groupResult.tags().get("department"));
        Assert.assertEquals("tagvalue", groupResult.tags().get("tagname"));
        Assert.assertTrue(region.name().equalsIgnoreCase(groupResult.regionName()));

        // Check existence
        Assert.assertTrue(resourceGroups.checkExistence(rgName));

        // Get
        ResourceGroup getGroup = resourceGroups.getByName(rgName);
        Assert.assertNotNull(getGroup);
        Assert.assertEquals(rgName, getGroup.name());
        // Update
        ResourceGroup updatedGroup = getGroup.update()
                .withTag("tag1", "value1")
                .apply();
        Assert.assertEquals("value1", updatedGroup.tags().get("tag1"));
        Assert.assertTrue(region.name().equalsIgnoreCase(getGroup.regionName()));
        // Delete
        resourceGroups.deleteByName(rgName);
        Assert.assertFalse(resourceGroups.checkExistence(rgName));
    }
}
