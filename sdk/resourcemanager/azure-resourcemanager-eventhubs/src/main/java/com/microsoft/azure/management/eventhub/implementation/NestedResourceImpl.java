/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.ProxyResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.NestedResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;

/**
 *  The implementation for {@link NestedResource}.
 * (Internal use only)
 *
 * @param <FluentModelT> the fluent model of the nested resource
 * @param <InnerModelT> the inner model of the nested resource
 * @param <FluentModelImplT> the fluent model implementation of the nested resource
 */
@LangDefinition
public abstract class NestedResourceImpl<FluentModelT extends Indexable, InnerModelT extends ProxyResource, FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        HasInner<InnerModelT>,
        HasManager<EventHubManager>,
        NestedResource {
    protected final EventHubManager manager;

    NestedResourceImpl(final String name, final InnerModelT inner, EventHubManager manager) {
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
    public EventHubManager manager() {
        return this.manager;
    }
}
