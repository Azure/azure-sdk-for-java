package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import rx.Observable;

/**
 * The base class for all updatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 */
abstract public class UpdatableImpl<
        FluentModelT,
        InnerModelT,
        FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
    extends
        CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
    implements
        Updatable<FluentModelImplT>,
        Appliable<FluentModelT> {
    public UpdatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
    }

    @Override
    public final Observable<FluentModelT> createAsync() {
        throw new IllegalStateException("Internal Error: createAsync cannot be called from UpdatableImpl");
    }
}
