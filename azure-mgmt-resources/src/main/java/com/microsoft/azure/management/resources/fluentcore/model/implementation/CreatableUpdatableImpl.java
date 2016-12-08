/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

/**
 * The base class for all creatable and updatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the implementation type of the fluent model
 */
public abstract class CreatableUpdatableImpl<
            FluentModelT  extends Indexable,
            InnerModelT,
            FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements
            Appliable<FluentModelT>,
            Creatable<FluentModelT>,
            CreateUpdateTaskGroup.ResourceCreatorUpdator<FluentModelT> {
    /**
     * The name of the creatable, updatable resource.
     */
    private final String name;

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
        super(innerObject);
        this.name = name;
        createUpdateTaskGroup = new CreateUpdateTaskGroup<>(this.key(), this);
    }

    /**
     * @return the name of the creatable updatable resource.
     */
    public String name() {
        return this.name;
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
     * @param creatable the creatable dependency.
     */
    @SuppressWarnings("unchecked")
    protected void addCreatableDependency(Creatable<? extends Indexable> creatable) {
        CreateUpdateTaskGroup<FluentModelT> childGroup =
                ((CreateUpdateTaskGroup.ResourceCreatorUpdator<FluentModelT>) creatable).creatorUpdatorTaskGroup();
        childGroup.merge(this.createUpdateTaskGroup);
    }

    /**
     * Add an updatable resource dependency for this resource.
     *
     * @param appliable the appliable dependency.
     */
    @SuppressWarnings("unchecked")
    protected void addAppliableDependency(Appliable<? extends Indexable> appliable) {
        CreateUpdateTaskGroup<FluentModelT> childGroup =
                ((CreateUpdateTaskGroup.ResourceCreatorUpdator<FluentModelT>) appliable).creatorUpdatorTaskGroup();
        childGroup.merge(this.createUpdateTaskGroup);
    }

    /**
     * Default implementation of prepare().
     */
    @Override
    public void prepare() {
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
    public Observable<Indexable> createAsync() {
        return this.executeTaskGroupAsyncStreaming();
    }

    /**
     * Default implementation to create the resource asynchronously.
     *
     * @param callback the callback to handle success and failure
     * @return a handle to cancel the request
     */
    public ServiceCall<FluentModelT> createAsync(final ServiceCallback<FluentModelT> callback) {
        return observableToFuture(Utils.<FluentModelT>rootResource(createAsync()), callback);
    }

    /**
     * Default implementation of create().
     *
     * @return the created resource
     */
    @SuppressWarnings("unchecked")
    public FluentModelT create() {
        return Utils.<FluentModelT>rootResource(createAsync()).toBlocking().single();
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

    /**
     * Default implementation to update the resource asynchronously.
     *
     * @param callback the callback to handle success and failure
     * @return a handle to cancel the request
     */
    @Override
    public ServiceCall<FluentModelT> applyAsync(ServiceCallback<FluentModelT> callback) {
        return observableToFuture(applyAsync(), callback);
    }

    /**
     * Default implementation of apply().
     *
     * @return the created resource
     */
    @Override
    public FluentModelT apply() {
        return applyAsync().toBlocking().last();
    }

    /**
     * This is the default implementation of 'updateResourceAsync', it simply calls createResourceAsync()
     * since for most of the resource both create and update are handled by the same API call
     * (CreateOrUpdate). A resource that uses different API call for update should override updateResourceAsync.
     *
     * @return the updated resource.
     */
    @Override
    public Observable<FluentModelT> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @SuppressWarnings("unchecked")
    protected Observable<FluentModelT> executeTaskGroupAsync() {
        if (createUpdateTaskGroup.isPreparer()) {
            createUpdateTaskGroup.prepare();
            return createUpdateTaskGroup.executeAsync().last();
        }
        throw new IllegalStateException("Internal Error: executeTaskGroupAsync can be called only on preparer");
    }

    @SuppressWarnings("unchecked")
    protected Observable<Indexable> executeTaskGroupAsyncStreaming() {
        if (createUpdateTaskGroup.isPreparer()) {
            createUpdateTaskGroup.prepare();
            return createUpdateTaskGroup.executeAsync()
                    .map(new Func1<FluentModelT, Indexable>() {
                            @Override
                            public Indexable call(FluentModelT fluentModelT) {
                                return fluentModelT;
                            }
                        });
        }
        throw new IllegalStateException("Internal Error: executeTaskGroupAsync can be called only on preparer");
    }

    @SuppressWarnings("unchecked")
    protected Func1<InnerModelT, FluentModelT> innerToFluentMap(final FluentModelImplT fluentModelImplT) {
        return new Func1<InnerModelT, FluentModelT>() {
            @Override
            public FluentModelT call(InnerModelT innerModelT) {
                fluentModelImplT.setInner(innerModelT);
                return (FluentModelT) fluentModelImplT;
            }
        };
    }

    protected ServiceCall<FluentModelT> observableToFuture(
            Observable<FluentModelT> observable,
            final ServiceCallback<FluentModelT> callback) {
        return ServiceCall.create(
                observable.map(new Func1<FluentModelT, ServiceResponse<FluentModelT>>() {
                    @Override
                    public ServiceResponse<FluentModelT> call(FluentModelT fluentModelT) {
                        // TODO: When https://github.com/Azure/azure-sdk-for-java/issues/1029 is done, this map (and this method) can be removed
                        return new ServiceResponse<>(fluentModelT, null);
                    }
                }), callback
        );
    }
}
