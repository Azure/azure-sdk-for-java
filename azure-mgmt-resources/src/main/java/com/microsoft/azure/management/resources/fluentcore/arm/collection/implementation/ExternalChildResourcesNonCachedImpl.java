/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;

/**
 * Externalized non cache-able child resource collection abstract implementation.
 * (Internal use only)
 * <p>
 * An external child resource collection is considered as non cache-able when it is not present as an inline
 * property of its parent resource i.e. a separate call is required to fetch them.
 * Consider using the cached version {@link ExternalChildResourcesCachedImpl} if the external child resources
 * present in the parent payload, in case of update this can save addition GET operation required before the
 * child resource PUT if PATCH is not supported.
 *
 * @param <FluentModelTImpl> the implementation of {@param FluentModelT}
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing the child resource
 * @param <ParentImplT> <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
 * @param <ParentT> the parent interface
 */
public abstract class ExternalChildResourcesNonCachedImpl<
        FluentModelTImpl extends ExternalChildResourceImpl<FluentModelT, InnerModelT, ParentImplT, ParentT>,
        FluentModelT extends ExternalChildResource<FluentModelT, ParentT>,
        InnerModelT,
        ParentImplT extends ParentT,
        ParentT>
        extends ExternalChildResourceCollectionImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT, ParentT> {
    /**
     * Creates a new ExternalNonInlineChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourcesNonCachedImpl(ParentImplT parent, String childResourceName) {
        super(parent, childResourceName);
    }

    /**
     * Prepare a given model of an external child resource for create.
     *
     * @param model the model to track create changes
     * @return the external child resource prepared for create
     */
    protected final FluentModelTImpl prepareDefine(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            throw new IllegalArgumentException(pendingOperationMessage(model.name(), model.childResourceKey()));
        }
        model.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        this.childCollection.put(model.childResourceKey(), model);
        return model;
    }

    /**
     * Prepare a given model of an external child resource for update.
     *
     * @param model the model to track update changes
     * @return the external child resource prepared for update
     */
    protected final FluentModelTImpl prepareUpdate(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            throw new IllegalArgumentException(pendingOperationMessage(model.name(), model.childResourceKey()));
        }
        model.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeUpdated);
        this.childCollection.put(model.childResourceKey(), model);
        return model;
    }

    /**
     * Prepare a given model of an external child resource for remove.
     *
     * @param model the model representing child resource to remove
     */
    protected final void prepareRemove(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            throw new IllegalArgumentException(pendingOperationMessage(model.name(), model.childResourceKey()));
        }
        model.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
        this.childCollection.put(model.childResourceKey(), model);
    }

    @Override
    protected final boolean clearAfterCommit() {
        return true;
    }

    private String pendingOperationMessage(String name, String key) {
        return "There is already an operation pending on the child resource ('" + childResourceName + "') with name (key) '" + name + " (" + key + ")'";
    }
}
