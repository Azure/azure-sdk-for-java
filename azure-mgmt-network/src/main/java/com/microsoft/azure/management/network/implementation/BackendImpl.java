/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.Backend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link Backend}.
 */
class BackendImpl
    extends ChildResourceImpl<BackendAddressPoolInner, LoadBalancerImpl>
    implements
        Backend,
        Backend.Definition<LoadBalancer.DefinitionStages.WithBackendOrProbe>,
        Backend.UpdateDefinition<LoadBalancer.Update>,
        Backend.Update {

    protected BackendImpl(BackendAddressPoolInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public LoadBalancerImpl attach() {
        this.parent().withBackend(this);
        return this.parent();
    }
}
