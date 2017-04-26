/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import rx.Observable;
import rx.functions.Func1;

/**
 * Base implementation for Wrapper interface.
 *
 * @param <InnerT> wrapped type
 * @param <Impl> impl type
 */
public abstract class RefreshableWrapperImpl<InnerT, Impl>
        extends WrapperImpl<InnerT>
        implements Refreshable<Impl> {

    protected RefreshableWrapperImpl(InnerT innerObject) {
        super(innerObject);
    }

    @Override
    public final Impl refresh() {
        return this.refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<Impl> refreshAsync() {
        final RefreshableWrapperImpl<InnerT, Impl> self = this;

        return this.getInnerAsync().map(new Func1<InnerT, Impl>() {
            @Override
            public Impl call(InnerT innerT) {
                self.setInner(innerT);
                return (Impl) self;
            }
        });
    }

    protected abstract Observable<InnerT> getInnerAsync();
}
