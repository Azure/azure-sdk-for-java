/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.util.Map;

/**
 * Represents an instance of a role.
 */
public final class RoleInstance {
    private final String id;
    private final int faultDomain;
    private final int updateDomain;
    private final Map<String, RoleInstanceEndpoint> endpoints;
    private Role role;

    /**
     * Constructor
     * 
     * @param id
     * @param faultDomain
     * @param updateDomain
     * @param endpoints
     */
    RoleInstance(String id, int faultDomain, int updateDomain,
            Map<String, RoleInstanceEndpoint> endpoints) {
        this.id = id;
        this.faultDomain = faultDomain;
        this.updateDomain = updateDomain;
        this.endpoints = endpoints;
    }

    void setRole(Role role) {
        this.role = role;
    }

    /**
     * Returns an integer value that indicates the fault domain in which this
     * instance resides.
     * 
     * @return The fault domain in which this instance resides.
     */
    public int getFaultDomain() {
        return faultDomain;
    }

    /**
     * Returns the ID of this instance.
     * <p>
     * The returned ID is unique to the application domain of the role's
     * instance. If an instance is terminated and has been configured to restart
     * automatically, the restarted instance will have the same ID as the
     * terminated instance.
     * 
     * @return A <code>String</code> object that represents the ID of this
     *         instance.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns an integer value that indicates the update domain in which this
     * instance resides.
     * 
     * @return The update domain in which this instance resides.
     */
    public int getUpdateDomain() {
        return updateDomain;
    }

    /**
     * Returns the {@link Role} object associated with this instance.
     * 
     * @return The <code>Role</code> object associated with this instance
     */
    public Role getRole() {
        return role;
    }

    /**
     * Returns the set of endpoints associated with this role instance.
     * 
     * @return A <code>Map</code> object of {@link RoleInstanceEndpoint} objects
     *         that represent the set of endpoints associated with this role
     *         instance.
     */
    public Map<String, RoleInstanceEndpoint> getInstanceEndpoints() {
        return endpoints;
    }
}
