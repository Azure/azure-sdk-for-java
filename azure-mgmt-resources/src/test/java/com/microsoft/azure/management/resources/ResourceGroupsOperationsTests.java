package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.Page;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

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
        resourceManagementClient.getResourceGroups().createOrUpdate(rgName, group);
        // List
        Page<ResourceGroup> listResult = resourceManagementClient.getResourceGroups().list(null, null).getBody();
        ResourceGroup groupResult = null;
        for (ResourceGroup rg : listResult.getItems()) {
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
        ResourceGroup getGroup = resourceManagementClient.getResourceGroups().get(rgName).getBody();
        Assert.assertNotNull(getGroup);
        Assert.assertEquals(rgName, getGroup.getName());
        Assert.assertEquals(location, getGroup.getLocation());
        // Delete
        resourceManagementClient.getResourceGroups().delete(rgName);
        Assert.assertFalse(resourceManagementClient.getResourceGroups().checkExistence(rgName).getBody());
    }
}
