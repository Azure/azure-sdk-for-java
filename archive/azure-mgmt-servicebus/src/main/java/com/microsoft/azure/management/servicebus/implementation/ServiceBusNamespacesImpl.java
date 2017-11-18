/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.servicebus.CheckNameAvailabilityResult;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.ServiceBusNamespaces;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for ServiceBusNamespaces.
 */
@LangDefinition
class ServiceBusNamespacesImpl extends TopLevelModifiableResourcesImpl<
        ServiceBusNamespace,
        ServiceBusNamespaceImpl,
        NamespaceInner,
        NamespacesInner,
        ServiceBusManager>
        implements ServiceBusNamespaces {

    ServiceBusNamespacesImpl(NamespacesInner innerCollection, ServiceBusManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public ServiceBusNamespace.DefinitionStages.Blank define(String name) {
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
                return new CheckNameAvailabilityResultImpl(checkNameAvailabilityResultInner);
            }
        });
    }

    @Override
    public ServiceFuture<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback) {
        return ServiceFuture.fromBody(this.checkNameAvailabilityAsync(name), callback);
    }

    @Override
    protected ServiceBusNamespaceImpl wrapModel(String name) {
        return new ServiceBusNamespaceImpl(name,
                new NamespaceInner(),
                this.manager());
    }

    @Override
    protected ServiceBusNamespaceImpl wrapModel(NamespaceInner inner) {
        return new ServiceBusNamespaceImpl(inner.name(),
                inner,
                this.manager());
    }
}