/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * The base class for all creatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 */
public abstract class CreatableImpl<FluentModelT, InnerModelT>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements CreatableTaskGroup.RootResourceCreator {
    /**
     * The group of tasks to create this resource and creatable it depends on.
     */
    private CreatableTaskGroup creatableTaskGroup;

	protected CreatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
        creatableTaskGroup = new CreatableTaskGroup(name, (Creatable<?>) this, this);
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
            creatableTaskGroup.execute();
        } else {
            createResource();
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

    /**
     * Creates this resource.
     *
     * @throws Exception the exception
     */
    protected abstract void createResource() throws Exception;
}
