/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.servicebus.CheckNameAvailabilityResult;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.Namespaces;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for Namespaces.
 */
class NamespacesImpl extends TopLevelModifiableResourcesImpl<
        Namespace,
        NamespaceImpl,
        NamespaceInner,
        NamespacesInner,
        ServiceBusManager>
        implements Namespaces {

    NamespacesImpl(NamespacesInner innerCollection, ServiceBusManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public Namespace.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) {
        return this.checkNameAvailabilityAsync(name).toBlocking().last();
    }

    @Override
    public Observable<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
        return this.inner().checkNameAvailabilityMethodAsync(name).map(new Func1<CheckNameAvailabilityResultInner, CheckNameAvailabilityResult>() {
            @Override
            public CheckNameAvailabilityResult call(CheckNameAvailabilityResultInner checkNameAvailabilityResultInner) {
                return new CheckNameAvailabilityResult(checkNameAvailabilityResultInner);
            }
        });
    }

    @Override
    public ServiceFuture<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback) {
        return ServiceFuture.fromBody(this.checkNameAvailabilityAsync(name), callback);
    }

    @Override
    protected NamespaceImpl wrapModel(String name) {
        return new NamespaceImpl(name,
                new NamespaceInner(),
                this.manager());
    }

    @Override
    protected NamespaceImpl wrapModel(NamespaceInner inner) {
        return new NamespaceImpl(inner.name(),
                inner,
                this.manager());
    }
}