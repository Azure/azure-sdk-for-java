package com.microsoft.azure.management.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GenericResourcesTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;
    private static ResourceGroup resourceGroup;
    private static GenericResources genericResources;

    private static String resourceName = "rgweb953";
    private static String rgName = "javacsmrg720";
    private static String newRgName = "javacsmrg189";

    @BeforeClass
    public static void setup() throws Exception {
        createClient();
        resourceGroups = resourceClient.resourceGroups();
        genericResources = resourceClient.genericResources();
        resourceGroups.define(rgName)
                .withRegion(Region.US_WEST)
                .create();
        resourceGroups.define(newRgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .create();
        resourceGroup = resourceGroups.getByName(rgName);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceGroups.delete(newRgName);
        resourceGroups.delete(rgName);
    }

    @Test
    public void canCreateUpdateMoveResource() throws Exception {
        // Create
        GenericResource resource = genericResources.define(resourceName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withResourceType("sites")
                .withProviderNamespace("Microsoft.Web")
                .withoutPlan()
                .withApiVersion("2015-08-01")
                .withParentResource("")
                .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Shared\"}"))
                .create();
        //List
        List<GenericResource> resourceList = genericResources.listByGroup(rgName);
        boolean found = false;
        for (GenericResource gr: resourceList) {
            if (gr.name().equals(resource.name())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // Get
        Assert.assertNotNull(genericResources.get(rgName, resource.resourceProviderNamespace(), resource.parentResourceId(), resource.resourceType(), resource.name(), resource.apiVersion()));
        // Move
        genericResources.moveResources(rgName, resourceGroups.getByName(newRgName), Arrays.asList(resource.id()));
        Assert.assertFalse(genericResources.checkExistence(rgName, resource.resourceProviderNamespace(), resource.parentResourceId(), resource.resourceType(), resource.name(), resource.apiVersion()));
        resource = genericResources.get(newRgName, resource.resourceProviderNamespace(), resource.parentResourceId(), resource.resourceType(), resource.name(), resource.apiVersion());
        Assert.assertNotNull(resource);
        // Update
        resource.update()
                .withApiVersion("2015-08-01")
                .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Dynamic\"}"))
                .apply();
    }
}
