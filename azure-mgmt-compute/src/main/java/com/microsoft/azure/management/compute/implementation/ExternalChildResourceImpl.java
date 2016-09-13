package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import rx.Observable;

/**
 * Externalized child resource abstract implementation.
 * Inorder to be eligible for an external child resource following criteria must be satisfied:
 * 1. It's is always associated with a parent resource and has no existence without parent
 *    i.e. if you delete parent then child resource will be deleted automatically.
 * 2. Parent will contain collection of child resources. this is not a hard requirement.
 * 3. It's has an ID and can be created, updated, fetched and deleted independent of the parent
 *    i.e. CRUD on child resource does not require CRUD on the parent
 * (Internal use only)
 *
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing the child resource
 * @param <ParentImplT> the parent Azure resource class type of the child resource
 */
public abstract class ExternalChildResourceImpl<
        FluentModelT extends ExternalChildResource,
        InnerModelT,
        ParentImplT>
        extends
        IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT> {
    /**
     * State representing any pending action that needs to be performed on this child resource.
     */
    private State state = State.None;
    /**
     * The child resource name.
     */
    private final String name;
    /**
     * Reference to the parent of the child resource.
     */
    protected final ParentImplT parent;

    /**
     * Creates an instance of external child resource in-memory.
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

    /**
     * @return the resource name
     */
    public String name() {
        return this.name;
    }

    /**
     * @return the in-memory state of this child resource and state represents any pending action on the
     * child resource.
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
     * Creates this external child resource.
     *
     * @return the observable to track the create action
     */
    public abstract Observable<FluentModelT> createAsync();

    /**
     * Update this external child resource.
     *
     * @return the observable to track the update action
     */
    public abstract Observable<FluentModelT> updateAsync();

    /**
     * Delete this external child resource.
     *
     * @return the observable to track the delete action.
     */
    public abstract Observable<Void> deleteAsync();

    /**
     * The possible states of a child resource in-memory.
     */
    public enum State {
        /**
         * No action needs to be taken on resource.
         */
        None,
        /**
         * Child resource required to be created.
         */
        ToBeCreated,
        /**
         * Child resource required to be updated.
         */
        ToBeUpdated,
        /**
         * Child resource required to be deleted.
         */
        ToBeRemoved
    }
}