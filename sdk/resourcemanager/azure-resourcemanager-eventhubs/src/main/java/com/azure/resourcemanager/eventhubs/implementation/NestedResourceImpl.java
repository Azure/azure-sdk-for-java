// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.management.ProxyResource;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.azure.resourcemanager.eventhubs.models.NestedResource;

/**
 *  The implementation for {@link NestedResource}.
 * (Internal use only)
 *
 * @param <FluentModelT> the fluent model of the nested resource
 * @param <InnerModelT> the inner model of the nested resource
 * @param <FluentModelImplT> the fluent model implementation of the nested resource
 */
public abstract class NestedResourceImpl<
    FluentModelT extends Indexable,
    InnerModelT extends ProxyResource,
    FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
    extends CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
    implements HasManager<EventHubsManager>, NestedResource {
    protected final EventHubsManager manager;

    NestedResourceImpl(final String name, final InnerModelT inner, EventHubsManager manager) {
        super(name, inner);
        this.manager = manager;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String name() {
        if (this.inner().name() == null) {
            return super.name();
        } else {
            return this.inner().name();
        }
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public EventHubsManager manager() {
        return this.manager;
    }
}
