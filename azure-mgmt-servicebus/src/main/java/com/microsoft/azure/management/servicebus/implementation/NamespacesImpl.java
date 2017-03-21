/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.Namespaces;

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

    @Override
    public Namespace.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }
}