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
     * create this resource and creatable resources it depends on.
     * <p>
     * dependency resources will be created only if this is the root group otherwise
     * it creates the main resource.
     *
     * @throws Exception the exception
     */
    protected void creatablesCreate() throws Exception {
        if (creatableTaskGroup.isRoot()) {
            creatableTaskGroup.prepare();
            creatableTaskGroup.execute();
        } else {
            createResource();
        }
    }

    protected ServiceCall creatablesCreateAsync(ServiceCallback<Void> callback) {
        if (creatableTaskGroup.isRoot()) {
            creatableTaskGroup.prepare();
            return creatableTaskGroup.executeAsync(callback);
        } else {
            return createResourceAsync(callback);
        }
    }

    /**
     * add a creatable resource dependency for this resource.
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
     * @return the created resource
     * @throws Exception when anything goes wrong
     */
    @SuppressWarnings("unchecked")
    public FluentModelImplT create() throws Exception {
        if (creatableTaskGroup.isRoot()) {
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
        if (creatableTaskGroup.isRoot()) {
            creatableTaskGroup.prepare();
            return creatableTaskGroup.executeAsync(Utils.toVoidCallback((FluentModelT) this, callback));
        } else {
            return createResourceAsync(Utils.toVoidCallback((FluentModelT) this, callback));
        }
    }

    /**
     * Creates this resource.
     *
     * @throws Exception the exception
     */
    protected abstract void createResource() throws Exception;

    protected abstract ServiceCall createResourceAsync(ServiceCallback<Void> callback);
}
