/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.model.implementation;

import com.azure.management.resources.fluentcore.model.Refreshable;
import reactor.core.publisher.Mono;

/**
 * Base implementation for Wrapper interface.
 *
 * @param <InnerT> wrapped type
 * @param <Impl>   impl type
 */
public abstract class RefreshableWrapperImpl<InnerT, Impl>
        extends WrapperImpl<InnerT>
        implements Refreshable<Impl> {

    protected RefreshableWrapperImpl(InnerT innerObject) {
        super(innerObject);
    }

    @Override
    public final Impl refresh() {
        return this.refreshAsync().block();
    }

    @Override
    public Mono<Impl> refreshAsync() {
        final RefreshableWrapperImpl<InnerT, Impl> self = this;

        return this.getInnerAsync()
                .map(innerT -> {
                    self.setInner(innerT);
                    return (Impl) self;
                });
    }

    protected abstract Mono<InnerT> getInnerAsync();
}
