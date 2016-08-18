/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
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
public abstract class CreatableImpl<FluentModelT extends ResourceT, InnerModelT, FluentModelImplT, ResourceT>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements CreatorTaskGroup.ResourceCreator<ResourceT> {
    /**
     * The name of the creatable resource.
     */
    private String name;

    /**
     * The group of tasks to create this resource and it's dependencies.
     */
    private CreatorTaskGroup<ResourceT> creatorTaskGroup;

    protected CreatableImpl(String name, InnerModelT innerObject) {
        super(innerObject);
        this.name = name;
        creatorTaskGroup = new CreatorTaskGroup<>(this.key(), this);
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
        return this.creatorTaskGroup.createdResource(key);
    }

    /**
     * @return the name of the creatable resource.
     */
    public String name() {
        return this.name;
    }

    /**
     * Default implementation of create().
     *
     * @return the created resource
     * @throws Exception when anything goes wrong
     */
    @SuppressWarnings("unchecked")
    public FluentModelT create() throws Exception {
        if (creatorTaskGroup.isPreparer()) {
            creatorTaskGroup.prepare();
            creatorTaskGroup.execute();
            return (FluentModelT) this;
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
    public ServiceCall<FluentModelT> createAsync(ServiceCallback<FluentModelT> callback) {
        if (creatorTaskGroup.isPreparer()) {
            creatorTaskGroup.prepare();
            return (ServiceCall<FluentModelT>) creatorTaskGroup.executeAsync((ServiceCallback<ResourceT>) callback);
        }
        throw new IllegalStateException("Internal Error: createAsync can be called only on preparer");
    }

    /**
     * @return the task group associated with this creatable.
     */
    public CreatorTaskGroup<ResourceT> creatorTaskGroup() {
        return this.creatorTaskGroup;
    }
}
