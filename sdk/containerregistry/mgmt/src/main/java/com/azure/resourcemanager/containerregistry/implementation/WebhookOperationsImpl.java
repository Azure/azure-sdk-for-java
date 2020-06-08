// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.models.Webhook;
import com.azure.resourcemanager.containerregistry.models.WebhookOperations;
import reactor.core.publisher.Mono;

/** Represents a webhook collection associated with a container registry. */
public class WebhookOperationsImpl implements WebhookOperations {
    private final RegistryImpl containerRegistry;
    private final WebhooksClientImpl webhooksClient;

    WebhookOperationsImpl(RegistryImpl containerRegistry) {
        this.containerRegistry = containerRegistry;
        if (containerRegistry != null) {
            this.webhooksClient = new WebhooksClientImpl(containerRegistry.manager(), containerRegistry);
        } else {
            this.webhooksClient = null;
        }
    }

    @Override
    public Webhook get(final String webhookName) {
        return this.getAsync(webhookName).block();
    }

    @Override
    public Mono<Webhook> getAsync(final String webhookName) {
        if (this.containerRegistry == null) {
            return null;
        }
        return webhooksClient
            .getAsync(this.containerRegistry.resourceGroupName(), this.containerRegistry.name(), webhookName);
    }

    @Override
    public void delete(final String webhookName) {
        if (this.containerRegistry == null) {
            return;
        }
        this
            .webhooksClient
            .delete(this.containerRegistry.resourceGroupName(), this.containerRegistry.name(), webhookName);
    }

    @Override
    public Mono<Void> deleteAsync(final String webhookName) {
        if (this.containerRegistry == null) {
            return null;
        }
        return this
            .webhooksClient
            .deleteAsync(this.containerRegistry.resourceGroupName(), this.containerRegistry.name(), webhookName);
    }

    @Override
    public PagedIterable<Webhook> list() {
        if (this.containerRegistry == null) {
            return null;
        }
        return this.webhooksClient.list(this.containerRegistry.resourceGroupName(), this.containerRegistry.name());
    }

    @Override
    public PagedFlux<Webhook> listAsync() {
        if (this.containerRegistry == null) {
            return null;
        }
        return this.webhooksClient.listAsync(this.containerRegistry.resourceGroupName(), this.containerRegistry.name());
    }
}
