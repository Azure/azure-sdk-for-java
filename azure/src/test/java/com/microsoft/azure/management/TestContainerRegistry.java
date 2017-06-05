/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.implementation.RegistryListCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestContainerRegistry extends TestTemplate<Registry, Registries> {

    @Override
    public Registry createResource(Registries registries) throws Exception {
        final String newName = "registry" + this.testId;
        Registry registry = registries.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withNewStorageAccount("crsa" + this.testId)
                .withRegistryNameAsAdminUser()
                .create();

        Assert.assertTrue(registry.adminUserEnabled());
        Assert.assertEquals(registry.storageAccountName(), "crsa" + this.testId);

        RegistryListCredentials registryCredentials = registry.listCredentials();
        Assert.assertNotNull(registryCredentials);
        Assert.assertEquals(newName, registryCredentials.username());
        Assert.assertEquals(2, registryCredentials.passwords().size());
        return registry;
    }

    @Override
    public Registry updateResource(Registry resource) throws Exception {
        resource =  resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(Registry resource) {
        System.out.println(new StringBuilder().append("Regsitry: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .toString());
    }
}