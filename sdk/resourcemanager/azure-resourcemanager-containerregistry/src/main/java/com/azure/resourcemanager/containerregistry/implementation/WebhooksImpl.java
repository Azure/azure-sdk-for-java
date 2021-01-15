// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.Webhook;
import com.azure.resourcemanager.containerregistry.fluent.models.WebhookInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

/** Represents a webhook collection associated with a container registry. */
public class WebhooksImpl
    extends ExternalChildResourcesNonCachedImpl<WebhookImpl, Webhook, WebhookInner, RegistryImpl, Registry> {

    /**
     * Creates a new ExternalNonInlineChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected WebhooksImpl(RegistryImpl parent, String childResourceName) {
        super(parent, parent.taskGroup(), childResourceName);
    }

    WebhookImpl defineWebhook(String name) {
        return prepareInlineDefine(
            new WebhookImpl(name, this.getParent(), new WebhookInner(), this.getParent().manager())
                .setCreateMode(true));
    }

    WebhookImpl updateWebhook(String name) {
        return prepareInlineUpdate(
            new WebhookImpl(name, this.getParent(), new WebhookInner(), this.getParent().manager())
                .setCreateMode(false));
    }

    void withoutWebhook(String name) {
        prepareInlineRemove(
            new WebhookImpl(name, this.getParent(), new WebhookInner(), this.getParent().manager())
                .setCreateMode(false));
    }
}
