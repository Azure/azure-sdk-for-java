/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.management.containerregistry.Webhook;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

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
        return this.getAsync(resourceGroupName, registryName, webhookName).toBlocking().single();
    }

    @Override
    public Observable<Webhook> getAsync(final String resourceGroupName, final String registryName, final String webhookName) {
        final WebhooksClientImpl self = this;
        final WebhooksInner webhooksInner = this.containerRegistryManager.inner().webhooks();

        return webhooksInner.getAsync(resourceGroupName, registryName, webhookName)
            .map(new Func1<WebhookInner, WebhookImpl>() {
                @Override
                public WebhookImpl call(WebhookInner webhookInner) {
                    if (self.containerRegistry != null) {
                        return new WebhookImpl(webhookName, self.containerRegistry, webhookInner, self.containerRegistryManager);
                    } else {
                        return new WebhookImpl(resourceGroupName, registryName, webhookName, webhookInner, self.containerRegistryManager);
                    }
                }
            }).flatMap(new Func1<WebhookImpl, Observable<Webhook>>() {
                @Override
                public Observable<Webhook> call(WebhookImpl webhook) {
                    return webhook.setCallbackConfigAsync();
                }
            });
    }

    @Override
    public void delete(final String resourceGroupName, final String registryName, final String webhookName) {
        this.containerRegistryManager.inner().webhooks().delete(resourceGroupName, registryName, webhookName);
    }

    @Override
    public Completable deleteAsync(final String resourceGroupName, final String registryName, final String webhookName) {
        return this.containerRegistryManager.inner().webhooks().deleteAsync(resourceGroupName, registryName, webhookName).toCompletable();
    }

    @Override
    public PagedList<Webhook> list(final String resourceGroupName, final String registryName) {
        final WebhooksClientImpl self = this;
        final PagedListConverter<WebhookInner, Webhook> converter = new PagedListConverter<WebhookInner, Webhook>() {
            @Override
            public Webhook typeConvert(WebhookInner inner) {
                if (self.containerRegistry != null) {
                    return new WebhookImpl(inner.name(), self.containerRegistry, inner, self.containerRegistryManager).setCallbackConfigAsync().toBlocking().single();
                } else {
                    return new WebhookImpl(resourceGroupName, registryName, inner.name(), inner, self.containerRegistryManager);
                }
            }
        };

        return converter.convert(this.containerRegistryManager.inner().webhooks().list(resourceGroupName, registryName));
    }

    @Override
    public Observable<Webhook> listAsync(final String resourceGroupName, final String registryName) {
        final WebhooksClientImpl self = this;
        final WebhooksInner webhooksInner = this.containerRegistryManager.inner().webhooks();

        return webhooksInner.listAsync(resourceGroupName, registryName)
            .flatMap(new Func1<Page<WebhookInner>, Observable<WebhookInner>>() {
                @Override
                public Observable<WebhookInner> call(Page<WebhookInner> webhookInnerPage) {
                    return Observable.from(webhookInnerPage.items());
                }
            }).map(new Func1<WebhookInner, WebhookImpl>() {
                @Override
                public WebhookImpl call(WebhookInner inner) {
                    if (self.containerRegistry != null) {
                        return new WebhookImpl(inner.name(), self.containerRegistry, inner, self.containerRegistryManager);
                    } else {
                        return new WebhookImpl(resourceGroupName, registryName, inner.name(), inner, self.containerRegistryManager);
                    }
                }
            }).flatMap(new Func1<WebhookImpl, Observable<Webhook>>() {
                @Override
                public Observable<Webhook> call(WebhookImpl webhook) {
                    return webhook.setCallbackConfigAsync();
                }
            });
    }
}
