/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;
import java.util.EnumSet;

/**
 * Class representing an AccessPolicy entity
 * 
 * 
 */
public class AccessPolicyInfo {
    // TODO: Rework this as needed once we have a serialization
    // solution decided on.

    private String id;
    private Date created;
    private Date lastModified;
    private String name;
    private double durationInMinutes;
    private final EnumSet<AccessPolicyPermission> permissions = EnumSet.noneOf(AccessPolicyPermission.class);

    public AccessPolicyInfo(String id, String Name, Date created, Date lastModified, double durationInMinutes,
            int permissionBits) {

        this.id = id;
        this.created = created;
        this.lastModified = lastModified;
        this.durationInMinutes = durationInMinutes;
        permissions.addAll(AccessPolicyPermission.permissionsFromBits(permissionBits));
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created
     *            the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the durationInMinutes
     */
    public double getDurationInMinutes() {
        return durationInMinutes;
    }

    /**
     * @param durationInMinutes
     *            the durationInMinutes to set
     */
    public void setDurationInMinutes(double durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    /**
     * @return the permissions
     */
    public EnumSet<AccessPolicyPermission> getPermissions() {
        return permissions;
    }

}
