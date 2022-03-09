// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.exception.AggregatedManagementException;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

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
        TaskGroup.HasTaskGroup,
        CreateUpdateTask.ResourceCreatorUpdater<FluentModelT> {
    /**
     * The name of the creatable updatable model.
     */
    private final String name;
    /**
     * The group of tasks to create or update this model and it's dependencies.
     */
    private final TaskGroup taskGroup;

    /**
     * Creates CreatableUpdatableImpl.
     *
     * @param name the name of the model
     * @param innerObject the inner model object
     */
    protected CreatableUpdatableImpl(String name, InnerModelT innerObject) {
        this(name, UUID.randomUUID().toString(), innerObject);
    }

    /**
     * Creates CreatableUpdatableImpl.
     *
     * @param name the name of the model
     * @param key task group key for the creator updater of this model
     * @param innerObject the inner model object
     */
    protected CreatableUpdatableImpl(String name, String key, InnerModelT innerObject) {
        super(key, innerObject);
        this.name = name;
        taskGroup = new TaskGroup(this.key(),
                new CreateUpdateTask<FluentModelT>(this));
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public TaskGroup taskGroup() {
        return this.taskGroup;
    }

    /**
     * Add a dependency task item for this model.
     *
     * @param dependency the dependency task item.
     * @return key to be used as parameter to taskResult(string) method to retrieve result the task item
     */
    protected String addDependency(FunctionalTaskItem dependency) {
        Objects.requireNonNull(dependency);
        return this.taskGroup.addDependency(dependency);
    }

    /**
     * Add a dependency task group for this model.
     *
     * @param dependency the dependency.
     * @return key to be used as parameter to taskResult(string) method to retrieve result of root
     * task in the given dependency task group
     */
    protected String addDependency(TaskGroup.HasTaskGroup dependency) {
        Objects.requireNonNull(dependency);
        this.taskGroup.addDependencyTaskGroup(dependency.taskGroup());
        return dependency.taskGroup().key();
    }

    /**
     * Add a creatable dependency for this model.
     *
     * @param creatable the creatable dependency.
     * @return the key to be used as parameter to taskResult(string) method to retrieve created dependency
     */
    @SuppressWarnings("unchecked")
    protected String addDependency(Creatable<? extends Indexable> creatable) {
        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) creatable;
        return this.addDependency(dependency);
    }

    /**
     * Add an appliable dependency for this model.
     *
     * @param appliable the appliable dependency.
     * @return the key to be used as parameter to taskResult(string) method to retrieve updated dependency
     */
    @SuppressWarnings("unchecked")
    protected String addDependency(Appliable<? extends Indexable> appliable) {
        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) appliable;
        return this.addDependency(dependency);
    }

    /**
     * Add an executable dependency for this model.
     *
     * @param executable the executable dependency
     * @return the key to be used as parameter to taskResult(string) method to retrieve result of executing
     * the executable dependency
     */
    @SuppressWarnings("unchecked")
    protected String addDependency(Executable<? extends Indexable> executable) {
        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) executable;
        return this.addDependency(dependency);
    }

    /**
     * Add a "post-run" dependent task item for this model.
     *
     * @param dependent the "post-run" dependent task item.
     * @return key to be used as parameter to taskResult(string) method to retrieve result of root
     * task in the given dependent task group
     */
    public String addPostRunDependent(FunctionalTaskItem dependent) {
        Objects.requireNonNull(dependent);
        return this.taskGroup().addPostRunDependent(dependent);
    }

    /**
     * Add a "post-run" dependent for this model.
     *
     * @param dependent the "post-run" dependent.
     * @return key to be used as parameter to taskResult(string) method to retrieve result of root
     * task in the given dependent task group
     */
    protected String addPostRunDependent(TaskGroup.HasTaskGroup dependent) {
        Objects.requireNonNull(dependent);
        this.taskGroup.addPostRunDependentTaskGroup(dependent.taskGroup());
        return dependent.taskGroup().key();
    }

    /**
     * Add a creatable "post-run" dependent for this model.
     *
     * @param creatable the creatable "post-run" dependent.
     * @return the key to be used as parameter to taskResult(string) method to retrieve created "post-run" dependent
     */
    @SuppressWarnings("unchecked")
    protected String addPostRunDependent(Creatable<? extends Indexable> creatable) {
        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) creatable;
        return this.addPostRunDependent(dependency);
    }

    /**
     * Add an appliable "post-run" dependent for this model.
     *
     * @param appliable the appliable "post-run" dependent.
     * @return the key to be used as parameter to taskResult(string) method to retrieve updated "post-run" dependent
     */
    @SuppressWarnings("unchecked")
    protected String addPostRunDependent(Appliable<? extends Indexable> appliable) {
        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) appliable;
        return this.addPostRunDependent(dependency);
    }

    /**
     * Add an executable "post-run" dependent for this model.
     *
     * @param executable the executable "post-run" dependent
     * @return the key to be used as parameter to taskResult(string) method to retrieve result of executing
     * the executable "post-run" dependent
     */
    @SuppressWarnings("unchecked")
    protected void addPostRunDependent(Executable<? extends Indexable> executable) {
        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) executable;
        this.addPostRunDependent(dependency);
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        // The types extending from this type, can override this method and add
        // delayed dependencies and/or post-run dependents.
    }

    @Override
    public Mono<FluentModelT> createAsync() {
        return createOrUpdateAsync();
    }

    @Override
    public Mono<FluentModelT> applyAsync() {
        return createOrUpdateAsync();
    }

    @Override
    public Mono<FluentModelT> createAsync(Context context) {
        return invokeTaskGroupAsync(context);
    }

    @Override
    public Mono<FluentModelT> applyAsync(Context context) {
        return invokeTaskGroupAsync(context);
    }

    @SuppressWarnings("unchecked")
    public Mono<FluentModelT> createOrUpdateAsync() {
        return taskGroup.invokeAsync()
            .map(indexable -> (FluentModelT) indexable)
            .onErrorMap(AggregatedManagementException::convertToManagementException);
    }

    @SuppressWarnings("unchecked")
    private Mono<FluentModelT> invokeTaskGroupAsync(Context context) {
        return taskGroup().invokeAsync()
            .contextWrite(c -> c.putAll(FluxUtil.toReactorContext(context).readOnly()))
            .map(indexable -> (FluentModelT) indexable)
            .onErrorMap(AggregatedManagementException::convertToManagementException);
    }

    @Override
    public boolean isHot() {
        // createResourceAsync & updateResourceAsync returns cold observable since Retrofit Http
        // request APIs (POST, PUT, PATCH ..) returns cold observable
        return false;
    }

    @Override
    public FluentModelT create() {
        return createAsync().block();
    }

    @Override
    public FluentModelT apply() {
        return applyAsync().block();
    }

    @Override
    public FluentModelT create(Context context) {
        return createAsync(context).block();
    }

    @Override
    public FluentModelT apply(Context context) {
        return applyAsync(context).block();
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
    public Mono<FluentModelT> updateResourceAsync() {
        return this.createResourceAsync();
    }

    /**
     * Get result of one of the task that belongs to this task's task group.
     *
     * @param key the task key
     * @param <T> the actual type of the task result
     * @return the task result, null will be returned if task has not produced a result yet
     */
    @SuppressWarnings("unchecked")
    protected <T extends Indexable> T taskResult(String key) {
        Indexable result = this.taskGroup.taskResult(key);
        if (result == null) {
            return null;
        } else {
            T castedResult = (T) result;
            return castedResult;
        }
    }

    @SuppressWarnings("unchecked")
    protected Function<InnerModelT, FluentModelT> innerToFluentMap(final FluentModelImplT fluentModelImplT) {
        return innerModel -> {
            fluentModelImplT.setInner(innerModel);
            return (FluentModelT) fluentModelImplT;
        };
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        // The types extending from this type can override this method and perform
        // any activities that needs to be done after the processing of all
        // post-run tasks.
        return Mono.empty();
    }
}
