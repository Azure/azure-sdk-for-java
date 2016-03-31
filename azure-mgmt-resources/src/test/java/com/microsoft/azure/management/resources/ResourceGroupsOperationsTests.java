package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.models.ResourceGroup;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class ResourceGroupsOperationsTests extends ResourceManagementTestBase {
    @Test
    public void canCreateResourceGroup() throws Exception {
        String rgName = "javacsmrg";
        String location = "southcentralus";
        // Create
        ResourceGroup group = new ResourceGroup();
        group.setLocation(location);
        group.setTags(new HashMap<String, String>());
        group.getTags().put("department", "finance");
        group.getTags().put("tagname", "tagvalue");
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        // List
        List<ResourceGroup> listResult = resourceManagementClient.resourceGroups().list().getBody();
        ResourceGroup groupResult = null;
        for (ResourceGroup rg : listResult) {
            if (rg.getName().equals(rgName)) {
                groupResult = rg;
                break;
            }
        }
        Assert.assertNotNull(groupResult);
        Assert.assertEquals("finance", groupResult.getTags().get("department"));
        Assert.assertEquals("tagvalue", groupResult.getTags().get("tagname"));
        Assert.assertEquals(location, groupResult.getLocation());
        // Get
        ResourceGroup getGroup = resourceManagementClient.resourceGroups().get(rgName).getBody();
        Assert.assertNotNull(getGroup);
        Assert.assertEquals(rgName, getGroup.getName());
        Assert.assertEquals(location, getGroup.getLocation());
        // Delete
        resourceManagementClient.resourceGroups().delete(rgName);
        Assert.assertFalse(resourceManagementClient.resourceGroups().checkExistence(rgName).getBody());
    }
}
