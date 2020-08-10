// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.WebhooksClient;
import com.azure.resourcemanager.containerregistry.models.Registries;
import com.azure.resourcemanager.containerregistry.models.Webhook;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

/** Represents a webhook collection associated with a container registry. */
public class WebhooksClientImpl implements Registries.WebhooksClient {
    private final ContainerRegistryManager containerRegistryManager;
    private final RegistryImpl containerRegistry;

    WebhooksClientImpl(ContainerRegistryManager containerRegistryManager, RegistryImpl containerRegistry) {
        this.containerRegistryManager = containerRegistryManager;
        this.containerRegistry = containerRegistry;
    }

    @Override
    public Webhook get(final String resourceGroupName, final String registryName, final String webhookName) {
        return this.getAsync(resourceGroupName, registryName, webhookName).block();
    }

    @Override
    public Mono<Webhook> getAsync(final String resourceGroupName, final String registryName, final String webhookName) {
        final WebhooksClientImpl self = this;
        final WebhooksClient webhooksInner = this.containerRegistryManager.inner().getWebhooks();

        return webhooksInner
            .getAsync(resourceGroupName, registryName, webhookName)
            .map(
                webhookInner -> {
                    if (self.containerRegistry != null) {
                        return new WebhookImpl(
                            webhookName, self.containerRegistry, webhookInner, self.containerRegistryManager);
                    } else {
                        return new WebhookImpl(
                            resourceGroupName, registryName, webhookName, webhookInner, self.containerRegistryManager);
                    }
                })
            .flatMap(webhook -> webhook.setCallbackConfigAsync());
    }

    @Override
    public void delete(final String resourceGroupName, final String registryName, final String webhookName) {
        this.containerRegistryManager.inner().getWebhooks().delete(resourceGroupName, registryName, webhookName);
    }

    @Override
    public Mono<Void> deleteAsync(final String resourceGroupName, final String registryName, final String webhookName) {
        return this
            .containerRegistryManager
            .inner()
            .getWebhooks()
            .deleteAsync(resourceGroupName, registryName, webhookName);
    }

    @Override
    public PagedIterable<Webhook> list(final String resourceGroupName, final String registryName) {
        return new PagedIterable<>(this.listAsync(resourceGroupName, registryName));
    }

    @Override
    public PagedFlux<Webhook> listAsync(final String resourceGroupName, final String registryName) {
        final WebhooksClientImpl self = this;
        final WebhooksClient webhooksInner = this.containerRegistryManager.inner().getWebhooks();

        return PagedConverter
            .flatMapPage(
                webhooksInner
                    .listAsync(resourceGroupName, registryName)
                    .mapPage(
                        inner -> {
                            if (self.containerRegistry != null) {
                                return new WebhookImpl(
                                    inner.name(), self.containerRegistry, inner, self.containerRegistryManager);
                            } else {
                                return new WebhookImpl(
                                    resourceGroupName,
                                    registryName,
                                    inner.name(),
                                    inner,
                                    self.containerRegistryManager);
                            }
                        }),
                webhook -> webhook.setCallbackConfigAsync());
    }
}
