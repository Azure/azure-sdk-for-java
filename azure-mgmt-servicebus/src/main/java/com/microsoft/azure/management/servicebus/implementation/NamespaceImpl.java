/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.servicebus.*;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Implementation for Namespace.
 */
class NamespaceImpl extends GroupableResourceImpl<
        Namespace,
        NamespaceResourceInner,
        NamespaceImpl,
        ServiceBusManager>
        implements
        Namespace,
        Namespace.Definition,
        Namespace.Update {

    NamespaceImpl(String name, NamespaceResourceInner innerObject, ServiceBusManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public String dnsLabel() {
        return null;
    }

    @Override
    public String fqdn() {
        return null;
    }

    @Override
    public NamespaceSku sku() {
        return null;
    }

    @Override
    public DateTime createdAt() {
        return null;
    }

    @Override
    public DateTime updatedAt() {
        return null;
    }

    @Override
    public Queues queues() {
        return null;
    }

    @Override
    public Topics topics() {
        return null;
    }

    @Override
    public NamespaceAuthorizationRules authorizationRules() {
        return null;
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
    public Update withoutTopic(String name) {
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
    protected Observable<NamespaceResourceInner> getInnerAsync() {
        return null;
    }

    @Override
    public Observable<Namespace> createResourceAsync() {
        return null;
    }
}
