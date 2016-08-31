package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import rx.Observable;

/**
 * Externalized child resource abstract implementation.
 *
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing this child resource
 * @param <ParentImplT> the parent Azure resource class type of this child resource
 */
abstract class ExternalChildResourceImpl<
        FluentModelT extends ExternalChildResource,
        InnerModelT,
        ParentImplT>
        extends
        IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT> {
    private State state = State.None;
    private final String name;
    protected final ParentImplT parent;

    /**
     * Creates an external child resource.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     */
    protected ExternalChildResourceImpl(String name, ParentImplT parent, InnerModelT innerObject) {
        super(innerObject);
        this.name = name;
        this.parent = parent;
    }

    public String name() {
        return this.name;
    }

    /**
     * @return the in-memory state of this child resource
     */
    public State state() {
        return this.state;
    }

    /**
     * Update the in-memory state.
     *
     * @param newState the new state of this child resource
     */
    public void setState(State newState) {
        this.state = newState;
    }

    /**
     * Creates or update this external child resource.
     *
     * @return the observable to track the create or update action
     */
    public abstract Observable<FluentModelT> setAsync();

    /**
     * Delete this external child resource.
     *
     * @return the observable to track the delete action.
     */
    public abstract Observable<Void> deleteAsync();

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