/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.dag.TaskGroup;
import com.microsoft.azure.management.resources.fluentcore.dag.TaskGroupTerminateOnErrorStrategy;
import com.microsoft.azure.management.resources.fluentcore.dag.TaskItem;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Executable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;
import rx.functions.Func1;

/**
 * The base class for all creatable and updatable model.
 *
 * @param <FluentModelT> the fluent model type
 * @param <InnerModelT> the inner model type that the fluent model wraps
 * @param <FluentModelImplT> the implementation type of the fluent model
 */
public abstract class CreatableUpdatableImpl<
        FluentModelT extends Indexable,
        InnerModelT,
        FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements
        Appliable<FluentModelT>,
        Creatable<FluentModelT>,
        TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>>,
        CreateUpdateTask.ResourceCreatorUpdator<FluentModelT> {
    /**
     * The name of the creatable updatable model.
     */
    private final String name;
    /**
     * The group of tasks to create or update this model and it's dependencies.
     */
    private final TaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>> taskGroup;

    /**
     * Creates CreatableUpdatableImpl.
     *
     * @param name the name of the model
     * @param innerObject the inner model object
     */
    protected CreatableUpdatableImpl(String name, InnerModelT innerObject) {
        super(innerObject);
        this.name = name;
        taskGroup = new TaskGroup<>(this.key(),
                new CreateUpdateTask<>(this),
                TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_INPROGRESS_TASKS_COMPLETION);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public TaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>> taskGroup() {
        return this.taskGroup;
    }

    /**
     * Add a creatable dependency for the model.
     *
     * @param creatable the creatable dependency.
     */
    @SuppressWarnings("unchecked")
    protected void addCreatableDependency(Creatable<? extends Indexable> creatable) {
        TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>> childModel =
                (TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>>) creatable;
        childModel.taskGroup().merge(this.taskGroup);
    }

    /**
     * Add an updatable dependency for the model.
     *
     * @param appliable the appliable dependency.
     */
    @SuppressWarnings("unchecked")
    protected void addAppliableDependency(Appliable<? extends Indexable> appliable) {
        TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>> childModel =
                (TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>>) appliable;
        childModel.taskGroup().merge(this.taskGroup);
    }

    /**
     * Add an executable dependency for this executable model.
     *
     * @param executable the executable dependency
     */
    @SuppressWarnings("unchecked")
    protected void addExecutableDependency(Executable<? extends Indexable> executable) {
        TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>> childModel =
                (TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>>) executable;

        Creatable<FluentModelT> that = this;
        TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>> parentModel =
                (TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>>) that;

        childModel.taskGroup().merge(parentModel.taskGroup());
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean isHot() {
        // createResourceAsync & updateResourceAsync returns cold observable since Retrofit Http
        // request APIs (POST, PUT, PATCH ..) returns cold observable
        return false;
    }

    @Override
    public Observable<Indexable> createAsync() {
        return this.executeTaskGroupAsyncStreaming();
    }

    @Override
    public ServiceFuture<FluentModelT> createAsync(final ServiceCallback<FluentModelT> callback) {
        return ServiceFuture.fromBody(Utils.<FluentModelT>rootResource(createAsync()), callback);
    }

    @Override
    public FluentModelT create() {
        return Utils.<FluentModelT>rootResource(createAsync()).toBlocking().single();
    }

    /**
     * Default implementation of {@link Updatable#update()}.
     *
     * @return the first stage of model update
     */
    @SuppressWarnings("unchecked")
    public FluentModelImplT update() {
        return (FluentModelImplT) this;
    }


    @Override
    public Observable<FluentModelT> applyAsync() {
        return this.executeTaskGroupAsync();
    }

    @Override
    public ServiceFuture<FluentModelT> applyAsync(ServiceCallback<FluentModelT> callback) {
        return ServiceFuture.fromBody(applyAsync(), callback);
    }

    @Override
    public FluentModelT apply() {
        return applyAsync().toBlocking().last();
    }

    @Override
    public Observable<FluentModelT> updateResourceAsync() {
        return this.createResourceAsync();
    }

    protected Observable<FluentModelT> executeTaskGroupAsync() {
        return taskGroup.executeAsync().last();
    }

    protected Observable<Indexable> executeTaskGroupAsyncStreaming() {
        return taskGroup.executeAsync()
                .map(new Func1<FluentModelT, Indexable>() {
                    @Override
                    public Indexable call(FluentModelT fluentModel) {
                        return fluentModel;
                    }
                });
    }

    protected FluentModelT createdModel(String key) {
        return this.taskGroup.taskResult(key);
    }

    @SuppressWarnings("unchecked")
    protected Func1<InnerModelT, FluentModelT> innerToFluentMap(final FluentModelImplT fluentModelImplT) {
        return new Func1<InnerModelT, FluentModelT>() {
            @Override
            public FluentModelT call(InnerModelT innerModel) {
                fluentModelImplT.setInner(innerModel);
                return (FluentModelT) fluentModelImplT;
            }
        };
    }

}