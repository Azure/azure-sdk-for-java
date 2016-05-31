/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
