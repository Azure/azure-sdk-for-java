// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtesting;

import org.junit.jupiter.api.Assertions;
import com.azure.resourcemanager.loadtesting.models.LoadTestResource;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentityType;

public class ResourceOperations {

    private String location;
    private String resourceGroupName;
    private String loadTestResourceName;

    public ResourceOperations(String location, String resourceGroupName, String loadTestResourceName) {
        this.location = location;
        this.resourceGroupName = resourceGroupName;
        this.loadTestResourceName = loadTestResourceName;
    }

    public void create(LoadTestManager manager) {
        LoadTestResource resource = manager
            .loadTests()
            .define(loadTestResourceName)
            .withRegion(location)
            .withExistingResourceGroup(resourceGroupName)
            .withDescription("This is new load test resource")
            .create();

        Assertions.assertEquals(loadTestResourceName, resource.name());
        Assertions.assertEquals(location, resource.regionName());
        Assertions.assertEquals(resourceGroupName, resource.resourceGroupName());
        Assertions.assertEquals("This is new load test resource", resource.description());
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals("Succeeded", resource.provisioningState().toString());
    }

    public void update(LoadTestManager manager) {
        LoadTestResource resourcePreUpdate = getResource(manager);

        LoadTestResource resourcePostUpdate = resourcePreUpdate
            .update()
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .apply();

        Assertions.assertEquals(resourcePreUpdate.name(), resourcePostUpdate.name());
        Assertions.assertEquals(resourcePreUpdate.regionName(), resourcePostUpdate.regionName());
        Assertions.assertEquals(resourcePreUpdate.resourceGroupName(), resourcePostUpdate.resourceGroupName());
        Assertions.assertEquals(resourcePreUpdate.description(), resourcePostUpdate.description());
        Assertions.assertEquals(ManagedServiceIdentityType.SYSTEM_ASSIGNED, resourcePostUpdate.identity().type());
        Assertions.assertNotNull(resourcePostUpdate.id());
        Assertions.assertEquals("Succeeded", resourcePostUpdate.provisioningState().toString());
    }

    private LoadTestResource getResource(LoadTestManager manager) {
        LoadTestResource resource = manager
            .loadTests()
            .getByResourceGroup(resourceGroupName, loadTestResourceName);
        return resource;
    }

    public void get(LoadTestManager manager) {
        LoadTestResource resource = getResource(manager);

        Assertions.assertEquals(loadTestResourceName, resource.name());
        Assertions.assertEquals(location, resource.regionName());
        Assertions.assertEquals(resourceGroupName, resource.resourceGroupName());
        Assertions.assertEquals("This is new load test resource", resource.description());
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals("Succeeded", resource.provisioningState().toString());
    }

    public void delete(LoadTestManager manager) {
        manager
            .loadTests()
            .deleteByResourceGroup(resourceGroupName, loadTestResourceName);
    }
}
