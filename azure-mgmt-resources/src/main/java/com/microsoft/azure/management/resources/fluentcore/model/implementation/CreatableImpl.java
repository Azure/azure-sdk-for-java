/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;
import rx.functions.Func1;

/**
 * The base class for all creatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 */
public abstract class CreatableImpl<FluentModelT, InnerModelT, FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements CreatorTaskGroup.ResourceCreator<FluentModelT> {
    /**
     * The name of the creatable resource.
     */
    private String name;

    /**
     * The group of tasks to create this resource and it's dependencies.
     */
    private CreatorTaskGroup<FluentModelT> creatorTaskGroup;

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
    protected void addCreatableDependency(Creatable<? extends Resource> creatableResource) {
        CreatorTaskGroup<FluentModelT> childGroup =
                ((CreatorTaskGroup.ResourceCreator<Resource>) creatableResource).creatorTaskGroup();
        childGroup.merge(this.creatorTaskGroup);
    }

    protected Resource createdResource(String key) {
        return (Resource) this.creatorTaskGroup.createdResource(key);
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
     * @return the handle to the create REST call
     */
    @SuppressWarnings("unchecked")
    public Observable<FluentModelT> createAsync() {
        if (creatorTaskGroup.isPreparer()) {
            creatorTaskGroup.prepare();
            return creatorTaskGroup.executeAsync();
        }
        throw new IllegalStateException("Internal Error: createAsync can be called only on preparer");
    }

    /**
     * @return the task group associated with this creatable.
     */
    public CreatorTaskGroup creatorTaskGroup() {
        return this.creatorTaskGroup;
    }

    protected Func1<InnerModelT, FluentModelT> innerToFluentMap(final FluentModelImplT fluentModelImplT) {
        return new Func1<InnerModelT, FluentModelT>() {
            @Override
            public FluentModelT call(InnerModelT innerModelT) {
                fluentModelImplT.setInner(innerModelT);
                return (FluentModelT) fluentModelImplT;
            }
        };
    }
}
