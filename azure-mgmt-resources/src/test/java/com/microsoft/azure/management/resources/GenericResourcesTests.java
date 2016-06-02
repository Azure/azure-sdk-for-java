package com.microsoft.azure.management.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

public class GenericResourcesTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;
    private static ResourceGroup resourceGroup;
    private static GenericResources genericResources;

    private static String resourceName = "rgweb1";
    private static String rgName = "javacsmrg6";
    private static String newRgName = "javacsmrg7";

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
        resourceGroup = resourceGroups.get(rgName);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceGroups.delete(rgName);
        resourceGroups.delete(newRgName);
    }

    @Test
    public void canCreateUpdateMoveResource() throws Exception {
        // Create
        GenericResource resource = genericResources.define(resourceName)
                .withRegion(Region.US_WEST)
                .withExistingGroup(rgName)
                .withResourceType("sites")
                .withProviderNamespace("Microsoft.Web")
                .withParentResource("")
                .withoutPlan()
                .withApiVersion("2015-08-01")
                .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Shared\"}"))
                .create();
        Assert.assertNotNull(genericResources.get(rgName, resource.name()));
        // Move
        genericResources.moveResources(rgName, resourceGroups.get(newRgName), Arrays.asList(resource.id()));
        Assert.assertNull(genericResources.get(rgName, resource.name()));
        Assert.assertNotNull(genericResources.get(newRgName, resource.name()));
        // Update
    }
}
