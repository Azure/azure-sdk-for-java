package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;

/**
 * Externalized child resource collection abstract implementation.
 * (Internal use only)
 *
 * @param <FluentModelTImpl> the implementation of {@param FluentModelT}
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing the child resource
 * @param <ParentImplT> <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
 * @param <ParentT> the parent interface
 */
public class ExternalNonInlineChildResourcesImpl<
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
    protected ExternalNonInlineChildResourcesImpl(ParentImplT parent, String childResourceName) {
        super(parent, childResourceName);
    }

    /**
     * Prepare a given model of an external child resource for create.
     *
     * @param model the model to track create changes
     * @return the external child resource prepared for create
     */
    protected FluentModelTImpl prepareDefine(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            throw new IllegalArgumentException(pendingOperationMessage(model.name(), model.childResourceKey()));
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        this.childCollection.put(model.childResourceKey(), childResource);
        return childResource;
    }

    /**
     * Prepare a given model of an external child resource for update.
     *
     * @param model the model to track update changes
     * @return the external child resource prepared for update
     */
    protected FluentModelTImpl prepareUpdate(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            throw new IllegalArgumentException(pendingOperationMessage(model.name(), model.childResourceKey()));
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeUpdated);
        this.childCollection.put(model.childResourceKey(), childResource);
        return childResource;
    }

    /**
     * Prepare a given model of an external child resource for remove.
     *
     * @param model the model representing child resource to remove
     */
    protected void prepareRemove(FluentModelTImpl model) {
        FluentModelTImpl childResource = find(model.childResourceKey());
        if (childResource != null) {
            throw new IllegalArgumentException(pendingOperationMessage(model.name(), model.childResourceKey()));
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
        this.childCollection.put(model.childResourceKey(), childResource);
    }

    private String pendingOperationMessage(String name, String key) {
        return "There is already an operation pending on a child resource ('" + childResourceName + "') with name (key) '" + name + " (" + key + ")'";
    }
}
