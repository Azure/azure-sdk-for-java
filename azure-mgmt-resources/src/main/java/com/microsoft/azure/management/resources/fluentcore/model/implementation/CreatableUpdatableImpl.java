/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * The base class for all updatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the implementation type of the fluent model
 */
public abstract class CreatableUpdatableImpl<
            FluentModelT,
            InnerModelT,
            FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends CreatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
            Appliable<FluentModelT>,
            CreateUpdateTaskGroup.ResourceCreatorUpdator<FluentModelT> {
    /**
     * The group of tasks to create or update this resource and it's dependencies.
     */
    protected CreateUpdateTaskGroup<FluentModelT> createUpdateTaskGroup;

    /**
     * Creates a new instance of CreatableUpdatableImpl.
     *
     * @param name the name of the resource
     * @param innerObject the inner object
     */
    protected CreatableUpdatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
        createUpdateTaskGroup = new CreateUpdateTaskGroup<>(this.key(), this);
    }

    /**
     * @return the task group containing the tasks that creates or updates resources that this
     * resource depends on.
     */
    @Override
    public CreateUpdateTaskGroup<FluentModelT> creatorUpdatorTaskGroup() {
        return this.createUpdateTaskGroup;
    }

    /**
     * Gets a resource created by the task group belongs to this instance.
     *
     * @param key the key of the resource
     * @return the created resource.
     */
    protected Resource createdResource(String key) {
        return (Resource) this.createUpdateTaskGroup.createdResource(key);
    }

    /**
     * Add a creatable resource dependency for this resource.
     *
     * @param creatableResource the creatable dependency.
     */
    @SuppressWarnings("unchecked")
    protected void addCreatableDependency(Creatable<? extends Resource> creatableResource) {
        CreateUpdateTaskGroup<FluentModelT> childGroup =
                ((CreateUpdateTaskGroup.ResourceCreatorUpdator<FluentModelT>) creatableResource).creatorUpdatorTaskGroup();
        childGroup.merge(this.createUpdateTaskGroup);
    }

    /**
     * Begins an update for a new resource.
     * <p>
     * This is the beginning of the builder pattern used to update top level resources
     * in Azure. The final method completing the definition and starting the actual resource creation
     * process in Azure is {@link Appliable#apply()}.
     *
     * @return the stage of new resource update
     */
    @SuppressWarnings("unchecked")
    public FluentModelImplT update() {
        return (FluentModelImplT) this;
    }

    /**
     * Default implementation of createAsync().
     *
     * @return the observable that emit the created resource.
     */
    @Override
    public Observable<FluentModelT> createAsync() {
        return this.executeTaskGroupAsync();
    }

    /**
     * Default implementation of applyAsync().
     *
     * @return the observable that emit the updated resource.
     */
    @Override
    public Observable<FluentModelT> applyAsync() {
        return this.executeTaskGroupAsync();
    }

    @Override
    public ServiceCall<FluentModelT> applyAsync(ServiceCallback<FluentModelT> callback) {
        return observableToFuture(applyAsync(), callback);
    }

    @Override
    public FluentModelT apply() {
        return applyAsync().toBlocking().last();
    }

    @SuppressWarnings("unchecked")
    protected Observable<FluentModelT> executeTaskGroupAsync() {
        if (createUpdateTaskGroup.isPreparer()) {
            createUpdateTaskGroup.prepare();
            return createUpdateTaskGroup.executeAsync().last();
        }
        throw new IllegalStateException("Internal Error: executeTaskGroupAsync can be called only on preparer");
    }
}
