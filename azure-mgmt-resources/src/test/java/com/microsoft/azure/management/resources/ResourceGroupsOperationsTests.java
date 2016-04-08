package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import org.junit.Assert;
import org.junit.Test;

public class ResourceGroupsOperationsTests extends ResourceManagementTestBase {
    public ResourceGroupsOperationsTests() throws Exception {
    }

    @Test
    public void canCreateResourceGroup() throws Exception {
        String rgName = "javacsmrg";
        String location = "southcentralus";
        // Create
        subscription.resourceGroups().define(rgName)
                .withLocation(Region.US_WEST)
                .withTag("department", "finance")
                .withTag("tagname", "tagvalue")
                .provision();
        // List
        ResourceGroups resourceGroups = subscription.resourceGroups();
        ResourceGroup groupResult = null;
        for (ResourceGroup rg : resourceGroups) {
            if (rg.name().equals(rgName)) {
                groupResult = rg;
                break;
            }
        }
        Assert.assertNotNull(groupResult);
        Assert.assertEquals("finance", groupResult.tags().get("department"));
        Assert.assertEquals("tagvalue", groupResult.tags().get("tagname"));
        Assert.assertEquals(location, groupResult.location());
        // Get
        ResourceGroup getGroup = resourceGroups.get(rgName);
        Assert.assertNotNull(getGroup);
        Assert.assertEquals(rgName, getGroup.name());
        Assert.assertEquals(location, getGroup.location());
        // Delete
        resourceGroups.delete(rgName);
        Assert.assertFalse(resourceGroups.checkExistence(rgName));
    }
}
