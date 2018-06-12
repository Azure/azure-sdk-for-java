/**
 * Copyright Microsoft Corporation
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

/**
 * Permissions available to an access policy
 * 
 */
public enum AccessPolicyPermission {
    NONE(0), READ(1), WRITE(2), DELETE(4), LIST(8);

    private int flagValue;

    private AccessPolicyPermission(int value) {
        flagValue = value;
    }

    /**
     * Get the flag bit value associated with this permission
     * 
     * @return The integer permission value
     */
    public int getFlagValue() {
        return flagValue;
    }

    /**
     * Given an integer representing the permissions as a bit vector, convert it
     * into an <code>EnumSet&lt;AccessPolicyPermission&gt;</code> object
     * containing the correct permissions *
     * 
     * @param bits
     *            The bit vector of permissions
     * @return The set of permissions in an <code>EnumSet</code> object.
     */
    public static EnumSet<AccessPolicyPermission> permissionsFromBits(int bits) {
        EnumSet<AccessPolicyPermission> perms = EnumSet
                .of(AccessPolicyPermission.NONE);

        for (AccessPolicyPermission p : AccessPolicyPermission.values()) {
            if ((bits & p.getFlagValue()) != 0) {
                perms.remove(AccessPolicyPermission.NONE);
                perms.add(p);
            }
        }

        return perms;
    }

    /**
     * Convert an <code>EnumSet</code> containing permissions into the
     * corresponding integer bit vector to be passed to Media services.
     * 
     * @param perms
     *            The permissions
     * @return The bit vector to go out over the wire.
     */
    public static int bitsFromPermissions(EnumSet<AccessPolicyPermission> perms) {
        int result = 0;
        for (AccessPolicyPermission p : perms) {
            result |= p.getFlagValue();
        }

        return result;
    }
}
