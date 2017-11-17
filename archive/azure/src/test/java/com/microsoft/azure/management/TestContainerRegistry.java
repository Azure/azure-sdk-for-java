/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.RegistryCredentials;
import com.microsoft.azure.management.containerregistry.Webhook;
import com.microsoft.azure.management.containerregistry.WebhookAction;
import com.microsoft.azure.management.containerregistry.WebhookStatus;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestContainerRegistry extends TestTemplate<Registry, Registries> {

    @Override
    public Registry createResource(Registries registries) throws Exception {
        final String newName = "acr" + this.testId;
        final String rgName = "rgacr" + this.testId;
        Registry registry = registries.define(newName + "1")
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withClassicSku()
                .withNewStorageAccount("crsa" + this.testId)
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        Assert.assertTrue(registry.adminUserEnabled());
        Assert.assertEquals(registry.storageAccountName(), "crsa" + this.testId);

        RegistryCredentials registryCredentials = registry.getCredentials();
        Assert.assertNotNull(registryCredentials);
        Assert.assertEquals(newName + "1", registryCredentials.username());
        Assert.assertEquals(2, registryCredentials.accessKeys().size());
        Assert.assertTrue(registry.webhooks().list().isEmpty());

        Registry registry2 = registries.define(newName + "2")
            .withRegion(Region.US_WEST_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withBasicSku()
            .withRegistryNameAsAdminUser()
            .defineWebhook("webhookbing1")
                .withTriggerWhen(WebhookAction.PUSH, WebhookAction.DELETE)
                .withServiceUri("https://www.bing.com")
                .withRepositoriesScope("")
                .withTag("tag", "value")
                .withCustomHeader("name", "value")
                .attach()
            .defineWebhook("webhookbing2")
                .withTriggerWhen(WebhookAction.PUSH)
                .withServiceUri("https://www.bing.com")
                .withDefaultStatus(WebhookStatus.DISABLED)
                .withRepositoriesScope("")
                .withTag("tag", "value")
                .withCustomHeader("name", "value")
                .attach()
            .withTag("tag1", "value1")
            .create();

        Assert.assertTrue(registry2.adminUserEnabled());

        RegistryCredentials registryCredentials2 = registry2.getCredentials();
        Assert.assertNotNull(registryCredentials2);
        Assert.assertEquals(newName + "2", registryCredentials2.username());
        Assert.assertEquals(2, registryCredentials2.accessKeys().size());


        PagedList<Webhook> webhooksList = registry2.webhooks().list();

        Assert.assertFalse(webhooksList.isEmpty());
        Assert.assertEquals(2, webhooksList.size());
        Webhook webhook = registry2.webhooks()
            .get("webhookbing1");
        Assert.assertTrue(webhook.isEnabled());
        Assert.assertTrue(webhook.tags().containsKey("tag"));
        Assert.assertEquals("https://www.bing.com", webhook.serviceUri());
        Assert.assertTrue(webhook.isEnabled());
        Assert.assertEquals(2, webhook.triggers().size());

        webhook = registries.webhooks()
            .get(rgName, registry2.name(), "webhookbing2");
        Assert.assertFalse(webhook.isEnabled());
        Assert.assertTrue(webhook.tags().containsKey("tag"));
        Assert.assertEquals("https://www.bing.com", webhook.serviceUri());
        Assert.assertFalse(webhook.isEnabled());
        Assert.assertEquals(1, webhook.triggers().size());
        Assert.assertEquals(WebhookAction.PUSH, webhook.triggers().toArray()[0]);

        Registry registry3 = registries.getById(webhook.parentId());

        return registry3;
    }

    @Override
    public Registry updateResource(Registry resource) throws Exception {
        resource.update()
            .withoutWebhook("webhookbing1")
            .defineWebhook("webhookms")
                .withTriggerWhen(WebhookAction.PUSH, WebhookAction.DELETE)
                .withServiceUri("https://www.microsoft.com")
                .withRepositoriesScope("")
                .withDefaultStatus(WebhookStatus.ENABLED)
                .attach()
            .updateWebhook("webhookbing2")
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

        Webhook webhook = resource.webhooks()
            .get("webhookbing2");
        Assert.assertFalse(webhook.tags().containsKey("tag"));
        Assert.assertTrue(webhook.tags().containsKey("tag2"));
        Assert.assertEquals("https://www.bing.com/maps", webhook.serviceUri());
        Assert.assertFalse(webhook.isEnabled());
        Assert.assertEquals(1, webhook.triggers().size());
        Assert.assertEquals(WebhookAction.DELETE, webhook.triggers().toArray()[0]);

        webhook.refresh();
        webhook.enable();
        Assert.assertTrue(webhook.isEnabled());

        webhook.update()
            .withCustomHeader("header1", "value1")
            .withDefaultStatus(WebhookStatus.DISABLED)
            .withServiceUri("https://www.msn.com")
            .withRepositoriesScope("")
            .withTriggerWhen(WebhookAction.PUSH)
            .withoutTag("tag2")
            .withTag("tag3", "value")
            .apply();

        Assert.assertFalse(webhook.isEnabled());
        Assert.assertTrue(webhook.tags().containsKey("tag3"));
        Assert.assertEquals("https://www.msn.com", webhook.serviceUri());
        Assert.assertFalse(webhook.isEnabled());
        Assert.assertEquals(1, webhook.triggers().size());
        Assert.assertEquals(WebhookAction.PUSH, webhook.triggers().toArray()[0]);

        webhook.ping();
        Assert.assertNotNull(webhook.listEvents());

        resource.webhooks()
            .delete("webhookbing2");

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