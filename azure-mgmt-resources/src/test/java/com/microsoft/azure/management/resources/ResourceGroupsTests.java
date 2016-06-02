package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceGroupsTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;

    @BeforeClass
    public static void setup() throws Exception {
        createClient();
        resourceGroups = resourceClient.resourceGroups();
    }

    @Test
    public void canCreateResourceGroup() throws Exception {
        String rgName = "javacsmrg2";
        String location = "southcentralus";
        // Create
        resourceGroups.define(rgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withTag("department", "finance")
                .withTag("tagname", "tagvalue")
                .create();
        // List
        ResourceGroup groupResult = null;
        for (ResourceGroup rg : resourceGroups.list()) {
            if (rg.name().equals(rgName)) {
                groupResult = rg;
                break;
            }
        }
        Assert.assertNotNull(groupResult);
        Assert.assertEquals("finance", groupResult.tags().get("department"));
        Assert.assertEquals("tagvalue", groupResult.tags().get("tagname"));
        Assert.assertEquals(location, groupResult.region());
        // Get
        ResourceGroup getGroup = resourceGroups.get(rgName);
        Assert.assertNotNull(getGroup);
        Assert.assertEquals(rgName, getGroup.name());
        // Update
        ResourceGroup updatedGroup = getGroup.update()
                .withTag("tag1", "value1")
                .apply();
        Assert.assertEquals("value1", updatedGroup.tags().get("tag1"));
        Assert.assertEquals(location, getGroup.region());
        // Delete
        resourceGroups.delete(rgName);
        Assert.assertFalse(resourceGroups.checkExistence(rgName));
    }
}
