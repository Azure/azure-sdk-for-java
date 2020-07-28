// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;

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
 * @param <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
 * @param <ParentT> the parent interface
 */
public abstract class ExternalChildResourcesNonCachedImpl<
        FluentModelTImpl extends ExternalChildResourceImpl<FluentModelT, InnerModelT, ParentImplT, ParentT>,
        FluentModelT extends ExternalChildResource<FluentModelT, ParentT>,
        InnerModelT,
        ParentImplT extends ParentT,
        ParentT>
        extends ExternalChildResourceCollectionImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT, ParentT> {
    private ClientLogger logger = new ClientLogger(this.getClass());
    /**
     * Creates a new ExternalNonInlineChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param parentTaskGroup the TaskGroup the parent Azure resource belongs to
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourcesNonCachedImpl(ParentImplT parent,
                                                  TaskGroup parentTaskGroup, String childResourceName) {
        super(parent, parentTaskGroup, childResourceName);
    }

    /**
     * Prepare the given model of an external child resource for independent definition (without the parent context).
     *
     * @param model the model to prepare for independent create definition
     * @return the external child resource prepared for create
     */
    protected final FluentModelTImpl prepareIndependentDefine(FluentModelTImpl model) {
        model.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return model;
    }

    /**
     * Prepare the given model of an external child resource for inline create
     * (along with the definition or update of parent resource).
     *
     * @param model the model to track create changes
     * @return the external child resource prepared for create
     */
    protected final FluentModelTImpl prepareInlineDefine(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            pendingOperationException(model.name(), model.childResourceKey());
        }
        model.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        this.childCollection.put(model.childResourceKey(), model);
        return super.prepareForFutureCommitOrPostRun(model);
    }

    /**
     * Prepare the given model of an external child resource for inline update
     * (along with the definition or update of parent resource).
     *
     * @param model the model to track update changes
     * @return the external child resource prepared for update
     */
    protected final FluentModelTImpl prepareInlineUpdate(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            pendingOperationException(model.name(), model.childResourceKey());
        }
        model.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeUpdated);
        this.childCollection.put(model.childResourceKey(), model);
        return super.prepareForFutureCommitOrPostRun(model);
    }

    /**
     * Prepare the given model of an external child resource for inline removal
     * (along with the definition or update of parent resource).
     *
     * @param model the model representing child resource to remove
     */
    protected final void prepareInlineRemove(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            pendingOperationException(model.name(), model.childResourceKey());
        }
        model.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
        this.childCollection.put(model.childResourceKey(), model);
        super.prepareForFutureCommitOrPostRun(model);
    }

    @Override
    protected final boolean clearAfterCommit() {
        return true;
    }

    private void pendingOperationException(String name, String key) {
        String errorMessage = String.format(
            "There is already an operation pending on the child resource ('%s') with name (key) '%s (%s)'",
            childResourceName, name, key);
        throw logger.logExceptionAsError(new IllegalArgumentException(errorMessage));
    }
}
