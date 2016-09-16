/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import rx.Observable;

/**
 * The base class for all creatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 */
abstract public class CreatableImpl<
        FluentModelT,
        InnerModelT,
        FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends
        CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        Creatable<FluentModelT> {
    public CreatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
    }

    @Override
    public final Observable<FluentModelT> applyAsync() {
        throw new IllegalStateException("Internal Error: updateAsync cannot be called from CreatableImpl");
    }
}
