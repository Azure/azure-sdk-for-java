/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.containerregistry.Webhook;
import com.microsoft.azure.management.containerregistry.WebhookOperations;
import rx.Completable;
import rx.Observable;

/**
 * Represents a webhook collection associated with a container registry.
 */
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
        return this.getAsync(webhookName).toBlocking().single();
    }

    @Override
    public Observable<Webhook> getAsync(final String webhookName) {
        if (this.containerRegistry == null) {
            return null;
        }
        return webhooksClient.getAsync(this.containerRegistry.resourceGroupName(), this.containerRegistry.name(), webhookName);
    }

    @Override
    public void delete(final String webhookName) {
        if (this.containerRegistry == null) {
            return;
        }
        this.webhooksClient.delete(this.containerRegistry.resourceGroupName(), this.containerRegistry.name(), webhookName);
    }

    @Override
    public Completable deleteAsync(final String webhookName) {
        if (this.containerRegistry == null) {
            return null;
        }
        return this.webhooksClient.deleteAsync(this.containerRegistry.resourceGroupName(), this.containerRegistry.name(), webhookName);
    }

    @Override
    public PagedList<Webhook> list() {
        if (this.containerRegistry == null) {
            return null;
        }
        return this.webhooksClient.list(this.containerRegistry.resourceGroupName(), this.containerRegistry.name());
    }

    @Override
    public Observable<Webhook> listAsync() {
        if (this.containerRegistry == null) {
            return null;
        }
        return this.webhooksClient.listAsync(this.containerRegistry.resourceGroupName(), this.containerRegistry.name());
    }
}
