/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.servicebus.*;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Implementation for Namespace.
 */
@LangDefinition
class NamespaceImpl extends GroupableResourceImpl<
        Namespace,
        NamespaceResourceInner,
        NamespaceImpl,
        ServiceBusManager>
        implements
        Namespace,
        Namespace.Definition,
        Namespace.Update {

    NamespaceImpl(String name, NamespaceResourceInner inner, ServiceBusManager manager) {
        super(name, inner, manager);
    }

    @Override
    public String dnsLabel() {
        return this.inner().name();
    }

    @Override
    public String fqdn() {
        return this.inner().serviceBusEndpoint();
    }

    @Override
    public NamespaceSku sku() {
        return new NamespaceSku(this.inner().sku());
    }

    @Override
    public DateTime createdAt() {
        return this.inner().createdAt();
    }

    @Override
    public DateTime updatedAt() {
        return this.inner().updatedAt();
    }

    @Override
    public Queues queues() {
        return new QueuesImpl(this.resourceGroupName(),
                this.name(),
                this.manager());
    }

    @Override
    public Topics topics() {
        return new TopicsImpl(this.resourceGroupName(),
                this.name(),
                this.manager());
    }

    @Override
    public NamespaceAuthorizationRules authorizationRules() {
        return new NamespaceAuthorizationRulesImpl(this.resourceGroupName(),
                this.name(),
                manager());
    }

    @Override
    public NamespaceImpl withSku(NamespaceSku sku) {
        return this;
    }

    @Override
    public NamespaceImpl withNewQueue(String name, int maxSizeInMB) {
        return this;
    }

    @Override
    public NamespaceImpl withoutQueue(String name) {
        return this;
    }

    @Override
    public NamespaceImpl withNewTopic(String name, int maxSizeInMB) {
        return this;
    }

    @Override
    public NamespaceImpl withoutTopic(String name) {
        return this;
    }

    @Override
    public NamespaceImpl withNewAuthorizationRule(String name, AccessRights... rights) {
        return this;
    }

    @Override
    public NamespaceImpl withoutAuthorizationRule(String name) {
        return this;
    }

    @Override
    public Observable<Namespace> createResourceAsync() {
        return null;
    }

    @Override
    protected Observable<NamespaceResourceInner> getInnerAsync() {
        return null;
    }
}
