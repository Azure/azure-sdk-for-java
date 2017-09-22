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

    private void test(Registries registries) {
        Registry registry = registries.define("acr")
            .withRegion(Region.US_WEST)
            .withNewResourceGroup()
            .withBasicSku()
            .withRegistryNameAsAdminUser()
            .withTag("tag1", "value1")
            .defineWebhook("webhook")
                .withTriggerWhen(WebhookAction.PUSH)
                .withServiceUri("website")
                .withDefaultStatus(WebhookStatus.DISABLED)
                .withRepositoriesScope("")
                .withTag("tag", "value")
                .withCustomHeader("blah", "blah")
                .attach()
            .create();


        registry.update()
            .withoutWebhook("webhook")
            .withNewWebhook("blah2")
                .withTriggerWhen(WebhookAction.PUSH, WebhookAction.DELETE)
                .withServiceUri("blah")
                .withRepositoriesScope("")
                .withDefaultStatus(WebhookStatus.ENABLED)
                .attach()
            .updateWebhook("blah1")
                .withServiceUri("srv")
                .withTriggerWhen(WebhookAction.DELETE)
                .withCustomHeader("header", "value")
                .withoutTag("blah")
                .withTag("tag", "value")
                .parent()
            .withBasicSku()
            .withoutTag("tag")
            .withTag("tag2", "value")
            .apply();

        Webhook webhook = registry.getWebhook("blah2");

        webhook.refresh();
        webhook.update()
            .withCustomHeader("header1", "value1")
            .withDefaultStatus(WebhookStatus.DISABLED)
            .withServiceUri("something")
            .withRepositoriesScope("")
            .withTriggerWhen(WebhookAction.PUSH)
            .withoutTag("tag2")
            .withTag("tag", "value")
            .apply();

        Registry containerRegistry = registries.getByResourceGroup("test-acr", "test11223344");

        Collection<RegistryUsage> registryUsages = containerRegistry.listQuotaUsages();

//        List<OperationDefinitionInner> operations = containerRegistry.manager().inner().operations().list();
//
//        for (OperationDefinitionInner op : operations) {
//            System.out.format("Name: %s\n\tOp: %s\n\tProv: %s\n\tRes: %s\n\tDesc: %s\n", op.name(), op.display().operation(), op.display().provider(), op.display().resource(), op.display().description());
//        }

        List<Webhook> webhookList = containerRegistry.listWebhooks();
    }

    @Override
    public Registry createResource(Registries registries) throws Exception {
        final String newName = "registry" + this.testId;
        Registry registry = registries.define(newName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withClassicSku()
                .withNewStorageAccount("crsa" + this.testId)
                .withRegistryNameAsAdminUser()
                .create();

        Assert.assertTrue(registry.adminUserEnabled());
        Assert.assertEquals(registry.storageAccountName(), "crsa" + this.testId);

        RegistryCredentials registryCredentials = registry.getCredentials();
        Assert.assertNotNull(registryCredentials);
        Assert.assertEquals(newName, registryCredentials.username());
        Assert.assertEquals(2, registryCredentials.accessKeys().size());

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