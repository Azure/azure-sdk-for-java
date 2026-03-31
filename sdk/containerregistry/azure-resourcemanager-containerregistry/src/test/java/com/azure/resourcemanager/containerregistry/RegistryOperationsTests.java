// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry;

import com.azure.core.management.Region;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.WebhookAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryOperationsTests extends RegistryTest {

    private static final Region REGION = Region.US_WEST3;

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateContainerRegisterWithZoneRedundancy() {
        final String acrName = generateRandomResourceName("acr", 10);
        Registry registry = registryManager.containerRegistries()
            .define(acrName)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            .withPremiumSku()
            .withoutRegistryNameAsAdminUser()
            .withZoneRedundancy()
            .create();
        Assertions.assertTrue(registry.isZoneRedundancyEnabled());
    }

    @Test
    public void canCreateContainerRegistryWithWebhook() {
        final String acrName = generateRandomResourceName("acr", 10);
        Registry registry = registryManager.containerRegistries()
            .define(acrName)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            .withPremiumSku()
            .withoutRegistryNameAsAdminUser()
            .defineWebhook("acreventpush")
            .withTriggerWhen(WebhookAction.PUSH)
            .withServiceUri("https://contoso.com/acreventpush")
            .attach()
            .create();
        Assertions.assertEquals("https://contoso.com/acreventpush",
            registry.webhooks().get("acreventpush").serviceUri());

        registry.update()
            .defineWebhook("acreventdelete")
            .withTriggerWhen(WebhookAction.DELETE)
            .withServiceUri("https://contoso.com/acreventdelete")
            .attach()
            .apply();
        Assertions.assertEquals("https://contoso.com/acreventdelete",
            registry.webhooks().get("acreventdelete").serviceUri());
    }
}
