/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
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
 */
public abstract class CreatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements CreatableTaskGroup.RootResourceCreator {
    /**
     * The group of tasks to create this resource and creatable it depends on.
     */
    private CreatableTaskGroup creatableTaskGroup;

    protected CreatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
        creatableTaskGroup = new CreatableTaskGroup(name, (Creatable<? extends Resource>) this, this);
    }

    /**
     * Add a creatable resource dependency for this resource.
     *
     * @param creatableResource the creatable dependency.
     */
    protected void addCreatableDependency(Creatable<?> creatableResource) {
        CreatableTaskGroup childGroup = ((CreatableImpl) creatableResource).creatableTaskGroup;
        childGroup.merge(this.creatableTaskGroup);
    }

    @Override
    public void createRootResource() throws Exception {
        this.createResource();
    }

    @Override
    public ServiceCall createRootResourceAsync(ServiceCallback<Void> callback) {
        return this.createResourceAsync(callback);
    }

    protected Resource createdResource(String key) {
        return this.creatableTaskGroup.taskResult(key);
    }

    /**
     * Default implementation of create().
     *
     * @return the created resource
     * @throws Exception when anything goes wrong
     */
    @SuppressWarnings("unchecked")
    public FluentModelImplT create() throws Exception {
        // This method get's called in two ways:
        // 1. User explicitly call Creatable::create requesting creation of the resource.
        // 2. Gets called as a part of creating dependent resources for the resource user requested to create in #1.
        //
        // The creatableTaskGroup of the 'Creatable' on which user called 'create' (#1) is known as the preparer.
        // Preparer is the one responsible for preparing the underlying DAG for traversal.
        //
        // Initially creatableTaskGroup of all creatables as preparer, but as soon as user calls Create in one of
        // them (say A::Create) all other creatables that A depends on will be marked as non-preparer.
        //
        // This achieve two goals:
        //
        // a. When #2 happens we know group is already prepared and all left is to create the currently requested resource.
        // b. User can call 'Create' on any of the creatables not just the ROOT creatable. [ROOT is the one who does not
        //    have any dependent]
        //
        // After the creation of each resource in the creatableTaskGroup owned by the user chosen Creatable (#1), each
        // sub-creatableTaskGroup of the created resource will be marked back as preparer. Hence user can again call
        // Update on any of these resources [which is nothing but equivalent to calling create again]
        //
        if (creatableTaskGroup.isPreparer()) {
            creatableTaskGroup.prepare();
            creatableTaskGroup.execute();
        } else {
            createResource();
        }
        return (FluentModelImplT) this;
    }

    /**
     * Default implementation of createAsync().
     *
     * @param callback the callback to call on success or failure
     * @return the handle to the create REST call
     */
    @SuppressWarnings("unchecked")
    public ServiceCall createAsync(ServiceCallback<FluentModelT> callback) {
        if (creatableTaskGroup.isPreparer()) {
            creatableTaskGroup.prepare();
            return creatableTaskGroup.executeAsync(Utils.toVoidCallback((FluentModelT) this, callback));
        } else {
            return createResourceAsync(Utils.toVoidCallback((FluentModelT) this, callback));
        }
    }

    /**
     * Creates this resource.
     *
     * @throws Exception when anything goes wrong
     */
    protected abstract void createResource() throws Exception;

    /**
     * Creates this resource asynchronously.
     *
     * @throws Exception when anything goes wrong
     */
    protected abstract ServiceCall createResourceAsync(ServiceCallback<Void> callback);
}
