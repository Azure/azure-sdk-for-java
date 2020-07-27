// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link Indexable}, {@link Refreshable}, and {@link HasInner}.
 *
 * @param <FluentModelT> The fluent model type
 * @param <InnerModelT> Azure inner resource class type
 */
public abstract class IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        extends IndexableRefreshableImpl<FluentModelT>
        implements HasInner<InnerModelT> {

    private InnerModelT innerObject;

    protected IndexableRefreshableWrapperImpl(InnerModelT innerObject) {
        this.setInner(innerObject);
    }

    protected IndexableRefreshableWrapperImpl(String key, InnerModelT innerObject) {
        super(key);
        this.setInner(innerObject);
    }

    @Override
    public InnerModelT inner() {
        return this.innerObject;
    }

    /**
     * Set the wrapped inner model.
     * (For internal use only)
     *
     * @param inner the new inner model
     */
    public void setInner(InnerModelT inner) {
        this.innerObject = inner;
    }

    @Override
    public final FluentModelT refresh() {
        return refreshAsync().block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<FluentModelT> refreshAsync() {
        final IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT> self = this;
        return getInnerAsync().map(t -> {
            self.setInner(t);
            return (FluentModelT) self;
        });
    }

    protected abstract Mono<InnerModelT> getInnerAsync();
}
