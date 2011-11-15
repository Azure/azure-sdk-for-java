package com.microsoft.windowsazure.serviceruntime;

/**
 * Represents a change to the topology of the service.
 * <p>
 * The service's topology refers to the number of instances deployed for each
 * role that the service defines.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 * 
 * @author mariok
 */
public class RoleEnvironmentTopologyChange extends RoleEnvironmentChange {

    private String roleName;

    RoleEnvironmentTopologyChange(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Returns the name of the affected role.
     * 
     * @return A <code>String</code> object that represents the name of the
     *         affected role.
     */
    public String getRoleName() {
        return roleName;
    }

}
