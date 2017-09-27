/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.RegistryCredentials;
import com.microsoft.azure.management.containerregistry.RegistryUsage;
import com.microsoft.azure.management.containerregistry.Webhook;
import com.microsoft.azure.management.containerregistry.WebhookAction;
import com.microsoft.azure.management.containerregistry.WebhookStatus;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

import java.util.Collection;
import java.util.List;

public class TestContainerRegistry extends TestTemplate<Registry, Registries> {

    @Override
    public Registry createResource(Registries registries) throws Exception {
        final String newName = "acr" + this.testId;
        Registry registry = registries.define(newName + "1")
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup()
                .withClassicSku()
                .withNewStorageAccount("crsa" + this.testId)
                .withRegistryNameAsAdminUser()
                .defineWebhook("webhook-bing")
                    .withTriggerWhen(WebhookAction.PUSH)
                    .withServiceUri("https://www.bing.com")
                    .withDefaultStatus(WebhookStatus.ENABLED)
                    .withRepositoriesScope("")
                    .withTag("tag", "value")
                    .attach()
                .withTag("tag1", "value1")
                .create();

        Assert.assertTrue(registry.adminUserEnabled());
        Assert.assertEquals(registry.storageAccountName(), "crsa" + this.testId);

        RegistryCredentials registryCredentials = registry.getCredentials();
        Assert.assertNotNull(registryCredentials);
        Assert.assertEquals(newName, registryCredentials.username());
        Assert.assertEquals(2, registryCredentials.accessKeys().size());

        registry = registries.define(newName + "2")
            .withRegion(Region.US_WEST_CENTRAL)
            .withNewResourceGroup()
            .withBasicSku()
            .withRegistryNameAsAdminUser()
            .defineWebhook("webhook-bing1")
                .withTriggerWhen(WebhookAction.PUSH, WebhookAction.DELETE)
                .withServiceUri("https://www.bing.com")
                .withDefaultStatus(WebhookStatus.DISABLED)
                .withRepositoriesScope("")
                .withTag("tag", "value")
                .withCustomHeader("name", "value")
                .attach()
            .defineWebhook("webhook-bing2")
                .withTriggerWhen(WebhookAction.PUSH)
                .withServiceUri("https://www.bing.com")
                .withDefaultStatus(WebhookStatus.DISABLED)
                .withRepositoriesScope("")
                .withTag("tag", "value")
                .withCustomHeader("name", "value")
                .attach()
            .withTag("tag1", "value1")
            .create();

        Assert.assertTrue(registry.adminUserEnabled());

        registryCredentials = registry.getCredentials();
        Assert.assertNotNull(registryCredentials);
        Assert.assertEquals(newName, registryCredentials.username());
        Assert.assertEquals(2, registryCredentials.accessKeys().size());

        Webhook webhook = registry.getWebhook("webhook-bing1");
        Assert.assertFalse(webhook.isEnabled());
        Assert.assertTrue(webhook.tags().containsKey("tag"));

        return registry;
    }

    @Override
    public Registry updateResource(Registry resource) throws Exception {
        resource.update()
            .withoutWebhook("webhook-bing1")
            .withNewWebhook("webhook-ms")
                .withTriggerWhen(WebhookAction.PUSH, WebhookAction.DELETE)
                .withServiceUri("https://www.microsoft.com")
                .withRepositoriesScope("")
                .withDefaultStatus(WebhookStatus.ENABLED)
                .attach()
            .updateWebhook("webhook-bing2")
                .withServiceUri("https://www.bing.com/maps")
                .withTriggerWhen(WebhookAction.DELETE)
                .withCustomHeader("header", "value")
                .withoutTag("tag")
                .withTag("tag2", "value")
                .parent()
            .withStandardSku()
            .withoutTag("tag1")
            .withTag("tag2", "value")
            .apply();

        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertFalse(resource.tags().containsKey("tag1"));

        Webhook webhook = resource.getWebhook("webhook-bing2");
        webhook.refresh();
        webhook.update()
            .withCustomHeader("header1", "value1")
            .withDefaultStatus(WebhookStatus.DISABLED)
            .withServiceUri("https://www.msn.com")
            .withRepositoriesScope("")
            .withTriggerWhen(WebhookAction.PUSH)
            .withoutTag("tag2")
            .withTag("tag3", "value")
            .apply();

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