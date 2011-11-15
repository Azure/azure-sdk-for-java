package com.microsoft.windowsazure.serviceruntime;

import java.util.Map;

/**
 * Represents a role that is defined as part of a hosted service.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 * 
 * @author mariok
 */
public final class Role {
    private final String name;
    private final Map<String, RoleInstance> instances;

    /**
     * Constructor
     * 
     * @param name
     * @param instances
     */
    Role(String name, Map<String, RoleInstance> instances) {
        this.name = name;
        this.instances = instances;
    }

    /**
     * Returns the collection of instances for the role.
     * <p>
     * The number of instances of a role to be deployed to Windows Azure is
     * specified in the service's configuration file.
     * <p>
     * A role must define at least one internal endpoint in order for its set of
     * instances to be known at runtime.
     * 
     * @return A read-only <code>java.util.Map</code> object that contains the
     *         instances for the role, or <code>null</code> if the instances
     *         could not be retrieved.
     */
    public Map<String, RoleInstance> getInstances() {
        return instances;
    }

    /**
     * Returns the name of the role as it is declared in the service definition
     * file.
     * 
     * @return A <code>String</code> object that represents the name of the role
     *         as it is declared in the service definition file.
     */
    public String getName() {
        return name;
    }
}