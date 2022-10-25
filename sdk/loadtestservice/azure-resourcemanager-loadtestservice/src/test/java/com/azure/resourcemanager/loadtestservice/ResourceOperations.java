// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtestservice;

import java.util.Random;
import org.junit.jupiter.api.Assertions;
import com.azure.core.management.Region;
import com.azure.resourcemanager.loadtestservice.models.LoadTestResource;
import com.azure.resourcemanager.loadtestservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtestservice.models.ManagedServiceIdentityType;

public class ResourceOperations{

    private static final Random RANDOM = new Random();
    private static final Region Location = Region.US_WEST2;
    private static final String ResourceGroupName = "java-sdk-tests-rg";
    private static final String LoadTestResourceName = "loadtest-resource"+RANDOM.nextInt(1000);
    
    public static void create(LoadTestManager manager) {
        LoadTestResource resource = manager
        .loadTests()
        .define(LoadTestResourceName)
        .withRegion(Location.toString())
        .withExistingResourceGroup(ResourceGroupName)
        .withDescription("This is new load test resource")
        .create();

        Assertions.assertEquals(LoadTestResourceName, resource.name());
        Assertions.assertEquals(Location.toString(), resource.regionName());
        Assertions.assertEquals(ResourceGroupName, resource.resourceGroupName());
        Assertions.assertEquals("This is new load test resource", resource.description());
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals("Succeeded", resource.provisioningState().toString());
    }

    public static void update(LoadTestManager manager) {
        LoadTestResource resourcePreUpdate = GetResource(manager);
        resourcePreUpdate
        .update()
        .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
        .apply();
        LoadTestResource resourcePostUpdate = GetResource(manager);

        Assertions.assertEquals(resourcePreUpdate.name(), resourcePostUpdate.name());
        Assertions.assertEquals(resourcePreUpdate.regionName(), resourcePostUpdate.regionName());
        Assertions.assertEquals(resourcePreUpdate.resourceGroupName(), resourcePostUpdate.resourceGroupName());
        Assertions.assertEquals(resourcePreUpdate.description(), resourcePostUpdate.description());
        Assertions.assertEquals(ManagedServiceIdentityType.SYSTEM_ASSIGNED, resourcePostUpdate.identity().type());
        Assertions.assertNotNull(resourcePostUpdate.id());
        Assertions.assertEquals("Succeeded", resourcePostUpdate.provisioningState().toString());
    }

    private static LoadTestResource GetResource(LoadTestManager manager) {
        LoadTestResource resource = manager
        .loadTests().getByResourceGroup(ResourceGroupName, LoadTestResourceName);
        return resource;
    }

    public static void get(LoadTestManager manager) {
        LoadTestResource resource = GetResource(manager);

        Assertions.assertEquals(LoadTestResourceName, resource.name());
        Assertions.assertEquals(Location.toString(), resource.regionName());
        Assertions.assertEquals(ResourceGroupName, resource.resourceGroupName());
        Assertions.assertEquals("This is new load test resource", resource.description());
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals("Succeeded", resource.provisioningState().toString());
    }

    public static void delete(LoadTestManager manager) {
        manager
        .loadTests()
        .deleteByResourceGroup(ResourceGroupName, LoadTestResourceName);
    }

}
