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
public class RegistriesWebhooksClientImpl implements Registries.WebhooksClient {
    private final ContainerRegistryManager containerRegistryManager;
    private final RegistryImpl containerRegistry;

    RegistriesWebhooksClientImpl(ContainerRegistryManager containerRegistryManager, RegistryImpl containerRegistry) {
        this.containerRegistryManager = containerRegistryManager;
        this.containerRegistry = containerRegistry;
    }

    @Override
    public Webhook get(final String resourceGroupName, final String registryName, final String webhookName) {
        return this.getAsync(resourceGroupName, registryName, webhookName).block();
    }

    @Override
    public Mono<Webhook> getAsync(final String resourceGroupName, final String registryName, final String webhookName) {
        final WebhooksClient webhooksInner = this.containerRegistryManager.serviceClient().getWebhooks();

        return webhooksInner
            .getAsync(resourceGroupName, registryName, webhookName)
            .map(
                webhookInner -> {
                    if (this.containerRegistry != null) {
                        return new WebhookImpl(
                            webhookName, this.containerRegistry, webhookInner, this.containerRegistryManager);
                    } else {
                        return new WebhookImpl(
                            resourceGroupName, registryName, webhookName, webhookInner, this.containerRegistryManager);
                    }
                })
            .flatMap(WebhookImpl::setCallbackConfigAsync);
    }

    @Override
    public void delete(final String resourceGroupName, final String registryName, final String webhookName) {
        this.containerRegistryManager.serviceClient().getWebhooks()
            .delete(resourceGroupName, registryName, webhookName);
    }

    @Override
    public Mono<Void> deleteAsync(final String resourceGroupName, final String registryName, final String webhookName) {
        return this
            .containerRegistryManager
            .serviceClient()
            .getWebhooks()
            .deleteAsync(resourceGroupName, registryName, webhookName);
    }

    @Override
    public PagedIterable<Webhook> list(final String resourceGroupName, final String registryName) {
        return new PagedIterable<>(this.listAsync(resourceGroupName, registryName));
    }

    @Override
    public PagedFlux<Webhook> listAsync(final String resourceGroupName, final String registryName) {
        final WebhooksClient webhooksInner = this.containerRegistryManager.serviceClient().getWebhooks();

        return PagedConverter
            .flatMapPage(
                PagedConverter.mapPage(webhooksInner
                    .listAsync(resourceGroupName, registryName),
                        inner -> {
                            if (this.containerRegistry != null) {
                                return new WebhookImpl(
                                    inner.name(), this.containerRegistry, inner, this.containerRegistryManager);
                            } else {
                                return new WebhookImpl(
                                    resourceGroupName,
                                    registryName,
                                    inner.name(),
                                    inner,
                                    this.containerRegistryManager);
                            }
                        }),
                WebhookImpl::setCallbackConfigAsync);
    }
}
