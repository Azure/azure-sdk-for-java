/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

/**
 * The base class for all creatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 * @param <ResourceT> the fluent model or one of the base interface of fluent model
 */
public abstract class CreatableImpl<FluentModelT, InnerModelT, FluentModelImplT, ResourceT>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements CreatorTaskGroup.ResourceCreator<ResourceT> {

    /**
     * The group of tasks to create this resource and it's dependencies.
     */
    private CreatorTaskGroup<ResourceT> creatorTaskGroup;

    protected CreatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
        creatorTaskGroup = new CreatorTaskGroup<>(name, this);
    }

    /**
     * Add a creatable resource dependency for this resource.
     *
     * @param creatableResource the creatable dependency.
     */
    @SuppressWarnings("unchecked")
    protected void addCreatableDependency(Creatable<? extends ResourceT> creatableResource) {
        CreatorTaskGroup<ResourceT> childGroup =
                ((CreatorTaskGroup.ResourceCreator<ResourceT>) creatableResource).creatorTaskGroup();
        childGroup.merge(this.creatorTaskGroup);
    }

    protected ResourceT createdResource(String key) {
        return this.creatorTaskGroup.taskResult(key);
    }

    /**
     * Default implementation of create().
     *
     * @return the created resource
     * @throws Exception when anything goes wrong
     */
    @SuppressWarnings("unchecked")
    public FluentModelImplT create() throws Exception {
        if (creatorTaskGroup.isPreparer()) {
            creatorTaskGroup.prepare();
            creatorTaskGroup.execute();
            return (FluentModelImplT) this;
        }
        throw new IllegalStateException("Internal Error: create can be called only on preparer");
    }

    /**
     * Default implementation of createAsync().
     *
     * @param callback the callback to call on success or failure
     * @return the handle to the create REST call
     */
    @SuppressWarnings("unchecked")
    public ServiceCall createAsync(ServiceCallback<FluentModelT> callback) {
        if (creatorTaskGroup.isPreparer()) {
            creatorTaskGroup.prepare();
            creatorTaskGroup.executeAsync(Utils.toVoidCallback((FluentModelT) this, callback));
            return creatorTaskGroup.parallelServiceCall();
        }
        throw new IllegalStateException("Internal Error: createAsync can be called only on preparer");
    }

    /**
     * @return the task group associated with this creatable.
     */
    public CreatorTaskGroup creatorTaskGroup() {
        return this.creatorTaskGroup;
    }
}
