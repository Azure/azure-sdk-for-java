/**
 * Copyright 2012 Microsoft Corporation
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

import java.util.EnumSet;

public class CreateAccessPolicyOptions {
    private final EnumSet<AccessPolicyPermission> permissions = EnumSet.noneOf(AccessPolicyPermission.class);

    public CreateAccessPolicyOptions() {
    }

    /**
     * Returns a live pointer to the underlying permissions set.
     * 
     * @return the permissions
     */
    public EnumSet<AccessPolicyPermission> getPermissions() {
        return permissions;
    }

    /**
     * Add the given permissions to this creation request
     * 
     * @param permissionsToAdd
     * @return the CreateAccessPolicyOptions object
     */
    public CreateAccessPolicyOptions addPermissions(EnumSet<AccessPolicyPermission> permissionsToAdd) {
        permissions.addAll(permissionsToAdd);
        return this;
    }

    /**
     * Add the given permissions to this creation request
     * 
     * @param permissionsToAdd
     *            varargs - permissions to add
     * @return the CreateAccessPolicyOptions object
     */
    public CreateAccessPolicyOptions addPermissions(AccessPolicyPermission... permissionsToAdd) {
        for (AccessPolicyPermission permission : permissionsToAdd) {
            permissions.add(permission);
        }
        return this;
    }

    /**
     * Remove the given permissions from this creation request
     * 
     * @param permissionsToRemove
     * @return the CreateAccessPolicyOptions object
     */
    public CreateAccessPolicyOptions removePermissions(EnumSet<AccessPolicyPermission> permissionsToRemove) {
        permissions.removeAll(permissionsToRemove);
        return this;
    }

    /**
     * Remove the given permissions from this creation request
     * 
     * @param permissionsToRemove
     *            vararg - permissions to remove
     * @return the CreateAccessPolicyOptions object
     */
    public CreateAccessPolicyOptions removePermissions(AccessPolicyPermission... permissionsToRemove) {
        for (AccessPolicyPermission permission : permissionsToRemove) {
            permissions.remove(permission);
        }
        return this;
    }
}
