package com.microsoft.windowsazure.serviceruntime;

/**
 * Represents the listener for the environment changed event.
 * <p>
 * The environment changed event is encapsulated in a {@link RoleEnvironmentChangedEvent} object.
 */
public interface RoleEnvironmentChangedListener {

    /**
     * Occurs after a change to the service configuration has been applied to
     * the running instances of the role.
     * 
     * @param event
     *            A {@link RoleEnvironmentChangedEvent} object that represents
     *            the environment changed event.
     * 
     * @see RoleEnvironmentChangingListener#roleEnvironmentChanging
     */
    public void roleEnvironmentChanged(RoleEnvironmentChangedEvent event);

}