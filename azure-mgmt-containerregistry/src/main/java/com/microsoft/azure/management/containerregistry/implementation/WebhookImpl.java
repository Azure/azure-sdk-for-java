/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerregistry.ProvisioningState;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.Webhook;
import com.microsoft.azure.management.containerregistry.WebhookAction;
import com.microsoft.azure.management.containerregistry.WebhookStatus;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation for Webhook.
 */
@LangDefinition
public class WebhookImpl
    extends ExternalChildResourceImpl<Webhook, WebhookInner, RegistryImpl, Registry>
    implements Webhook {

    private WebhooksInner innerOperations;
    private WebhookCreateParametersInner webhookCreateParametersInner;
    private WebhookUpdateParametersInner webhookUpdateParametersInner;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name        the name of this external child resource
     * @param parent      reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param innerOperations reference to the inner object that accesses web hook operations
     */
    WebhookImpl(String name, RegistryImpl parent, WebhookInner innerObject, WebhooksInner innerOperations) {
        super(name, parent, innerObject);
        this.innerOperations = innerOperations;
        this.webhookCreateParametersInner = null;
        this.webhookUpdateParametersInner = null;
    }

    @Override
    public boolean isEnabled() {
        return this.inner().status().equals(WebhookStatus.ENABLED);
    }

    @Override
    public String scope() {
        return this.inner().scope();
    }

    @Override
    public Collection<WebhookAction> actions() {
        return Collections.unmodifiableList(this.inner().actions());
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public Observable<Webhook> createAsync() {
        final WebhookImpl self = this;
        if (webhookCreateParametersInner != null) {
            return this.innerOperations.createAsync(this.parent().resourceGroupName(),
                    this.parent().name(),
                    this.name(),
                    this.webhookCreateParametersInner)
                .map(new Func1<WebhookInner, Webhook>() {
                    @Override
                    public Webhook call(WebhookInner inner) {
                        self.webhookCreateParametersInner = null;
                        self.setInner(inner);
                        return self;
                    }
                });
        } else {
            return Observable.just(this).map(new Func1<WebhookImpl, Webhook>() {
                @Override
                public Webhook call(WebhookImpl webhook) {
                    return webhook;
                }
            });
        }
    }

    @Override
    public Observable<Webhook> updateAsync() {
        final WebhookImpl self = this;
        if (webhookUpdateParametersInner != null) {
            return this.innerOperations.updateAsync(this.parent().resourceGroupName(),
                    this.parent().name(),
                    this.name(),
                    this.webhookUpdateParametersInner)
                .map(new Func1<WebhookInner, Webhook>() {
                    @Override
                    public Webhook call(WebhookInner inner) {
                        self.setInner(inner);
                        self.webhookUpdateParametersInner = null;
                        return self;
                    }
                });
        } else {
            return Observable.just(this).map(new Func1<WebhookImpl, Webhook>() {
                @Override
                public Webhook call(WebhookImpl webhook) {
                    return webhook;
                }
            });
        }
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.innerOperations.deleteAsync(this.parent().resourceGroupName(),
            this.parent().name(),
            this.name());
    }

    @Override
    protected Observable<WebhookInner> getInnerAsync() {
        return this.innerOperations.getAsync(this.parent().resourceGroupName(),
            this.parent().name(),
            this.name());
    }
}
