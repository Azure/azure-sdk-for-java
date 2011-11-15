package com.microsoft.windowsazure.serviceruntime;

import java.util.Collection;

/**
 * Occurs after a change to the service configuration has been applied to the
 * running instances of the role.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 * 
 * @author mariok
 * 
 */
public class RoleEnvironmentChangedEvent {

    private Collection<RoleEnvironmentChange> changes;

    /**
     * Constructor. Can only be called by the deserialization logic
     * 
     * @param changes
     */
    RoleEnvironmentChangedEvent(Collection<RoleEnvironmentChange> changes) {
        this.changes = changes;
    }

    /**
     * Returns a collection of the configuration changes that were applied to
     * the role instance.
     * 
     * @return A <code>java.util.Collection</code> object containing the
     *         {@link RoleEnvironmentChange} objects that represent the
     *         configuration changes that were applied to the role instance.
     * 
     * @see RoleEnvironmentChange
     */
    public Collection<RoleEnvironmentChange> getChanges() {
        return changes;
    }

}
