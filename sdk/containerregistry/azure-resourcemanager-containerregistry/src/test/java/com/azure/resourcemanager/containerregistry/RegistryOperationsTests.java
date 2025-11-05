// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry;

import com.azure.core.management.Region;
import com.azure.resourcemanager.containerregistry.models.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryOperationsTests extends RegistryTest {

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateContainerRegisterWithZoneRedundancy() {
        final String acrName = generateRandomResourceName("acr", 10);
        Registry registry = registryManager.containerRegistries()
            .define(acrName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withPremiumSku()
            .withoutRegistryNameAsAdminUser()
            .withZoneRedundancy()
            .create();
        Assertions.assertTrue(registry.isZoneRedundancyEnabled());
    }
}
