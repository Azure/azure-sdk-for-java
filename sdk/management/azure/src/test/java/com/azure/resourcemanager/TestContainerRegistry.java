// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.models.Registries;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.RegistryCredentials;
import com.azure.resourcemanager.containerregistry.models.Webhook;
import com.azure.resourcemanager.containerregistry.models.WebhookAction;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

public class TestContainerRegistry extends TestTemplate<Registry, Registries> {

    @Override
    public Registry createResource(Registries registries) throws Exception {
        final String testId = registries.manager().sdkContext().randomResourceName("", 8);
        final String newName = "acr" + testId;
        final String rgName = "rgacr" + testId;
        Registry registry =
            registries
                .define(newName + "1")
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withStandardSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        Assertions.assertTrue(registry.adminUserEnabled());

        RegistryCredentials registryCredentials = registry.getCredentials();
        Assertions.assertNotNull(registryCredentials);
        Assertions.assertEquals(newName + "1", registryCredentials.username());
        Assertions.assertEquals(2, registryCredentials.accessKeys().size());
        Assertions.assertEquals(0, registry.webhooks().list().stream().count());

        Registry registry2 =
            registries
                .define(newName + "2")
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
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
                .enabled(false)
                .withRepositoriesScope("")
                .withTag("tag", "value")
                .withCustomHeader("name", "value")
                .attach()
                .withTag("tag1", "value1")
                .create();

        Assertions.assertTrue(registry2.adminUserEnabled());

        RegistryCredentials registryCredentials2 = registry2.getCredentials();
        Assertions.assertNotNull(registryCredentials2);
        Assertions.assertEquals(newName + "2", registryCredentials2.username());
        Assertions.assertEquals(2, registryCredentials2.accessKeys().size());

        PagedIterable<Webhook> webhooksList = registry2.webhooks().list();

        Assertions.assertEquals(2, webhooksList.stream().count());
        Webhook webhook = registry2.webhooks().get("webhookbing1");
        Assertions.assertTrue(webhook.isEnabled());
        Assertions.assertTrue(webhook.tags().containsKey("tag"));
        Assertions.assertEquals("https://www.bing.com", webhook.serviceUri());
        Assertions.assertTrue(webhook.isEnabled());
        Assertions.assertEquals(2, webhook.triggers().size());

        webhook = registries.webhooks().get(rgName, registry2.name(), "webhookbing2");
        Assertions.assertFalse(webhook.isEnabled());
        Assertions.assertTrue(webhook.tags().containsKey("tag"));
        Assertions.assertEquals("https://www.bing.com", webhook.serviceUri());
        Assertions.assertFalse(webhook.isEnabled());
        Assertions.assertEquals(1, webhook.triggers().size());
        Assertions.assertEquals(WebhookAction.PUSH, webhook.triggers().toArray()[0]);

        Registry registry3 = registries.getById(webhook.parentId());

        return registry3;
    }

    @Override
    public Registry updateResource(Registry resource) throws Exception {
        resource
            .update()
            .withoutWebhook("webhookbing1")
            .defineWebhook("webhookms")
            .withTriggerWhen(WebhookAction.PUSH, WebhookAction.DELETE)
            .withServiceUri("https://www.microsoft.com")
            .withRepositoriesScope("")
            .enabled(true)
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

        Assertions.assertTrue(resource.tags().containsKey("tag2"));
        Assertions.assertFalse(resource.tags().containsKey("tag1"));

        Webhook webhook = resource.webhooks().get("webhookbing2");
        Assertions.assertFalse(webhook.tags().containsKey("tag"));
        Assertions.assertTrue(webhook.tags().containsKey("tag2"));
        Assertions.assertEquals("https://www.bing.com/maps", webhook.serviceUri());
        Assertions.assertFalse(webhook.isEnabled());
        Assertions.assertEquals(1, webhook.triggers().size());
        Assertions.assertEquals(WebhookAction.DELETE, webhook.triggers().toArray()[0]);

        webhook.refresh();
        webhook.enable();
        Assertions.assertTrue(webhook.isEnabled());

        webhook
            .update()
            .withCustomHeader("header1", "value1")
            .enabled(false)
            .withServiceUri("https://www.msn.com")
            .withRepositoriesScope("")
            .withTriggerWhen(WebhookAction.PUSH)
            .withoutTag("tag2")
            .withTag("tag3", "value")
            .apply();

        Assertions.assertFalse(webhook.isEnabled());
        Assertions.assertTrue(webhook.tags().containsKey("tag3"));
        Assertions.assertEquals("https://www.msn.com", webhook.serviceUri());
        Assertions.assertFalse(webhook.isEnabled());
        Assertions.assertEquals(1, webhook.triggers().size());
        Assertions.assertEquals(WebhookAction.PUSH, webhook.triggers().toArray()[0]);

        webhook.ping();
        Assertions.assertNotNull(webhook.listEvents());

        resource.webhooks().delete("webhookbing2");

        return resource;
    }

    @Override
    public void print(Registry resource) {
        System
            .out
            .println(
                new StringBuilder()
                    .append("Regsitry: ")
                    .append(resource.id())
                    .append("Name: ")
                    .append(resource.name())
                    .append("\n\tResource group: ")
                    .append(resource.resourceGroupName())
                    .append("\n\tRegion: ")
                    .append(resource.region())
                    .append("\n\tTags: ")
                    .append(resource.tags())
                    .toString());
    }
}
