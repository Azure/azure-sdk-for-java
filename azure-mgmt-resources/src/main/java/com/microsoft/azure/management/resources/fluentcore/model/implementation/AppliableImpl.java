package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import rx.Observable;

/**
 * The base class for all updatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 */
public abstract class AppliableImpl<
        FluentModelT  extends Indexable,
        InnerModelT,
        FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
    extends
        CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
    implements
        Updatable<FluentModelImplT>,
        Appliable<FluentModelT> {
    /**
     * Creates an AppliableImpl.
     *
     * @param name the appliable name
     * @param innerObject the inner object
     */
    protected AppliableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
    }

    @Override
    public final Observable<Indexable> createAsync() {
        throw new IllegalStateException("Internal Error: createAsyncStreaming cannot be called from UpdatableImpl");
    }

    @Override
    public final Observable<FluentModelT> createResourceAsync() {
        throw new IllegalStateException("Internal Error: createResourceAsync cannot be called from UpdatableImpl");
    }

    @Override
    public abstract Observable<FluentModelT> updateResourceAsync();
}
