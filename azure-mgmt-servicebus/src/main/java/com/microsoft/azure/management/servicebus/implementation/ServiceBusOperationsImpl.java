/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.servicebus.ServiceBusOperation;
import com.microsoft.azure.management.servicebus.ServiceBusOperations;
import rx.Observable;

/**
 * The implementation of ServiceBusOperations.
 */
@LangDefinition
class ServiceBusOperationsImpl extends ReadableWrappersImpl<ServiceBusOperation, ServiceBusOperationImpl, OperationInner>
        implements ServiceBusOperations {
    private final OperationsInner client;
    private final ServiceBusManager manager;

    ServiceBusOperationsImpl(OperationsInner client, ServiceBusManager manager) {
        this.client = client;
        this.manager = manager;
    }

    @Override
    public OperationsInner inner() {
        return this.client;
    }

    @Override
    public PagedList<ServiceBusOperation> list() {
        return wrapList(this.client.list());
    }

    @Override
    public Observable<ServiceBusOperation> listAsync() {
        return wrapPageAsync(client.listAsync());
    }

    @Override
    protected ServiceBusOperationImpl wrapModel(OperationInner inner) {
        if (inner == null) {
            return null;
        }
        return new ServiceBusOperationImpl(inner);
    }

    @Override
    public ServiceBusManager manager() {
        return this.manager;
    }
}
