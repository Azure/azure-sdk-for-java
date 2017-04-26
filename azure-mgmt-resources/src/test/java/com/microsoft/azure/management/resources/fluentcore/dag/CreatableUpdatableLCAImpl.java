/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreateUpdateTask;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;
import rx.functions.Func1;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

abstract class CreatableUpdatableLCAImpl<
        FluentModelT extends Indexable,
        InnerModelT>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements
        Creatable<FluentModelT>,
        TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>>,
        CreateUpdateTask.ResourceCreatorUpdator<FluentModelT> {
    private final String name;
    private final TaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>> taskGroup;

    protected CreatableUpdatableLCAImpl(String name, InnerModelT innerObject) {
        super(innerObject);
        this.name = name;
        taskGroup = new TaskGroup<>(this.key(),
                new CreateUpdateTask<>(this),
                TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_HITTING_LCA_TASK);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public TaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>> taskGroup() {
        return this.taskGroup;
    }

    @SuppressWarnings("unchecked")
    protected void addCreatableDependency(Creatable<? extends Indexable> creatable) {
        TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>> childModel =
                (TaskGroup.HasTaskGroup<FluentModelT, CreateUpdateTask<FluentModelT>>) creatable;
        childModel.taskGroup().merge(this.taskGroup);
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean isHot() {
        return false;
    }

    @Override
    public Observable<Indexable> createAsync() {
        return taskGroup.executeAsync()
                .map(new Func1<FluentModelT, Indexable>() {
                    @Override
                    public Indexable call(FluentModelT fluentModel) {
                        return fluentModel;
                    }
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentModelT create() {
        return Utils.<FluentModelT>rootResource(createAsync()).toBlocking().single();
    }

    @Override
    public Observable<FluentModelT> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public abstract Observable<FluentModelT> createResourceAsync();

    @Override
    public ServiceFuture<FluentModelT> createAsync(final ServiceCallback<FluentModelT> callback) {
        throw new NotImplementedException();
    }
}
