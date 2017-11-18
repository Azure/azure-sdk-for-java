/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import rx.Observable;
import rx.functions.Func1;

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
        return refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<FluentModelT> refreshAsync() {
        final IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT> self = this;
        return getInnerAsync().map(new Func1<InnerModelT, FluentModelT>() {
            @Override
            public FluentModelT call(InnerModelT innerModelT) {
                self.setInner(innerModelT);
                return (FluentModelT) self;
            }
        });
    }

    protected abstract Observable<InnerModelT> getInnerAsync();
}
