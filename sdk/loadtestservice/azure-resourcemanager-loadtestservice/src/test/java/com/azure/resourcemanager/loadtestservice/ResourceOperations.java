// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtestservice;

import org.junit.jupiter.api.Assertions;
import com.azure.resourcemanager.loadtestservice.models.LoadTestResource;
import com.azure.resourcemanager.loadtestservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtestservice.models.ManagedServiceIdentityType;

public class ResourceOperations{

    private String Location;
    private String ResourceGroupName;
    private String LoadTestResourceName;

    public ResourceOperations(String location, String resourceGroupName, String loadTestResourceName) {
        Location = location;
        ResourceGroupName = resourceGroupName;
        LoadTestResourceName = loadTestResourceName;
    }

    public void Create(LoadTestManager manager) {
        // Create a load test resource
        LoadTestResource resource = manager
        .loadTests()
        .define(LoadTestResourceName)
        .withRegion(Location)
        .withExistingResourceGroup(ResourceGroupName)
        .withDescription("This is new load test resource")
        .create();

        // Validate the fields
        Assertions.assertEquals(LoadTestResourceName, resource.name());
        Assertions.assertEquals(Location, resource.regionName());
        Assertions.assertEquals(ResourceGroupName, resource.resourceGroupName());
        Assertions.assertEquals("This is new load test resource", resource.description());
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals("Succeeded", resource.provisioningState().toString());
    }

    public void Update(LoadTestManager manager) {
        // Update the load test resource
        LoadTestResource resourcePreUpdate = GetResource(manager);
        resourcePreUpdate
        .update()
        .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
        .apply();
        LoadTestResource resourcePostUpdate = GetResource(manager);

        // Validate the fields
        Assertions.assertEquals(resourcePreUpdate.name(), resourcePostUpdate.name());
        Assertions.assertEquals(resourcePreUpdate.regionName(), resourcePostUpdate.regionName());
        Assertions.assertEquals(resourcePreUpdate.resourceGroupName(), resourcePostUpdate.resourceGroupName());
        Assertions.assertEquals(resourcePreUpdate.description(), resourcePostUpdate.description());
        Assertions.assertEquals(ManagedServiceIdentityType.SYSTEM_ASSIGNED, resourcePostUpdate.identity().type());
        Assertions.assertNotNull(resourcePostUpdate.id());
        Assertions.assertEquals("Succeeded", resourcePostUpdate.provisioningState().toString());
    }

    private LoadTestResource GetResource(LoadTestManager manager) {
        // Get the load test resource
        LoadTestResource resource = manager
        .loadTests().getByResourceGroup(ResourceGroupName, LoadTestResourceName);
        return resource;
    }

    public void Get(LoadTestManager manager) {
        // Get the load test resource
        LoadTestResource resource = GetResource(manager);

        // Validate the fields
        Assertions.assertEquals(LoadTestResourceName, resource.name());
        Assertions.assertEquals(Location, resource.regionName());
        Assertions.assertEquals(ResourceGroupName, resource.resourceGroupName());
        Assertions.assertEquals("This is new load test resource", resource.description());
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals("Succeeded", resource.provisioningState().toString());
    }

    public void Delete(LoadTestManager manager) {
        // Delete the load test resource
        manager
        .loadTests()
        .deleteByResourceGroup(ResourceGroupName, LoadTestResourceName);
    }

}
