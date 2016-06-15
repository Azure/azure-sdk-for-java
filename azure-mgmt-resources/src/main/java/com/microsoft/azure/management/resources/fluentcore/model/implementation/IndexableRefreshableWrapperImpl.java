/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * The implementation for {@link Indexable}, {@link Refreshable}, and {@link Wrapper}.
 *
 * @param <FluentModelT> The fluent model type
 * @param <InnerModelT> Azure inner resource class type
 */
public abstract class IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
    extends IndexableRefreshableImpl<FluentModelT>
    implements Wrapper<InnerModelT> {

    private InnerModelT innerObject;
    protected IndexableRefreshableWrapperImpl(String name, InnerModelT innerObject) {
        super(name);
        this.innerObject = innerObject;
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
}
