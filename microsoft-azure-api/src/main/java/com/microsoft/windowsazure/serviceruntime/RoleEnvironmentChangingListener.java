package com.microsoft.windowsazure.serviceruntime;

/**
 * Represents the listener for the environment changing event.
 * <p>
 * The environment changing event is encapsulated in an
 * {@link com.microsoft.windowsazure.serviceruntime.RoleEnvironmentChangingEvent} object.
 */
public interface RoleEnvironmentChangingListener {

    /**
     * Occurs before a change to the service configuration is applied to the
     * running instances of the role.
     * <p>
     * Service configuration changes are applied on-the-fly to running role instances. Configuration changes include
     * changes to the service configuration changes and changes to the number of instances in the service.
     * <p>
     * This event occurs after the new configuration file has been submitted to Windows Azure but before the changes
     * have been applied to each running role instance. This event can be cancelled for a given instance to prevent the
     * configuration change.
     * <p>
     * Note that cancelling this event causes the instance to be automatically recycled. When the instance is recycled,
     * the configuration change is applied when it restarts.
     * 
     * @param event
     *            A {@link RoleEnvironmentChangingEvent} object that represents
     *            the environment changing event.
     * 
     * @see RoleEnvironmentChangedListener#roleEnvironmentChanged
     */
    public void roleEnvironmentChanging(RoleEnvironmentChangingEvent event);

}
