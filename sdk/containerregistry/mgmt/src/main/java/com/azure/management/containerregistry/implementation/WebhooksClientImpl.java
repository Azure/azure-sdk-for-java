/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.containerregistry.Registries;
import com.azure.management.containerregistry.Webhook;
import com.azure.management.containerregistry.models.WebhooksInner;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

/**
 * Represents a webhook collection associated with a container registry.
 */
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
        final WebhooksInner webhooksInner = this.containerRegistryManager.inner().webhooks();

        return webhooksInner.getAsync(resourceGroupName, registryName, webhookName)
            .map(webhookInner -> {
                if (self.containerRegistry != null) {
                    return new WebhookImpl(webhookName, self.containerRegistry, webhookInner, self.containerRegistryManager);
                } else {
                    return new WebhookImpl(resourceGroupName, registryName, webhookName, webhookInner, self.containerRegistryManager);
                }
            }).flatMap(webhook -> webhook.setCallbackConfigAsync());
    }

    @Override
    public void delete(final String resourceGroupName, final String registryName, final String webhookName) {
        this.containerRegistryManager.inner().webhooks().delete(resourceGroupName, registryName, webhookName);
    }

    @Override
    public Mono<Void> deleteAsync(final String resourceGroupName, final String registryName, final String webhookName) {
        return this.containerRegistryManager.inner().webhooks().deleteAsync(resourceGroupName, registryName, webhookName);
    }

    @Override
    public PagedIterable<Webhook> list(final String resourceGroupName, final String registryName) {
        return new PagedIterable<>(this.listAsync(resourceGroupName, registryName));
    }

    @Override
    public PagedFlux<Webhook> listAsync(final String resourceGroupName, final String registryName) {
        final WebhooksClientImpl self = this;
        final WebhooksInner webhooksInner = this.containerRegistryManager.inner().webhooks();

        return PagedConverter.flatMapPage(webhooksInner.listAsync(resourceGroupName, registryName)
             .mapPage(inner -> {
                if (self.containerRegistry != null) {
                    return new WebhookImpl(inner.getName(), self.containerRegistry, inner, self.containerRegistryManager);
                } else {
                    return new WebhookImpl(resourceGroupName, registryName, inner.getName(), inner, self.containerRegistryManager);
                }
            }), webhook -> webhook.setCallbackConfigAsync());
    }
}
