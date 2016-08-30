package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ResourceImpl;

/**
 * Externalized child resource abstract implementation.
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 * @param <ParentT> the parent Azure resource class type
 */
public abstract class ExternalChildResourceImpl<
        FluentModelT extends Resource & ChildResource,
        InnerModelT extends com.microsoft.azure.Resource,
        FluentModelImplT extends ExternalChildResourceImpl<FluentModelT, InnerModelT, FluentModelImplT, ParentT>,
        ParentT extends Resource>
        extends
        ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT> {
    protected ParentT parent;
    private State state = State.None;

    protected ExternalChildResourceImpl(String name, ParentT parent, InnerModelT inner) {
        super(name, inner);
        this.parent = parent;
    }

    /**
     * @return the state of this child resource
     */
    public State state() {
        return this.state;
    }

    /**
     * Update the state.
     *
     * @param newState the new state of this child resource
     */
    public void setState(State newState) {
        this.state = newState;
    }

    /**
     * The possible state of an child resource in-memory.
     */
    enum State {
        None,
        ToBeCreated,
        ToBeUpdated,
        ToBeRemoved
    }
}