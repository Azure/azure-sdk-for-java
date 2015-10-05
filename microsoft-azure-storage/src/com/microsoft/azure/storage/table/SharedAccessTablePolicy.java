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
package com.microsoft.azure.storage.table;

import java.util.EnumSet;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.SharedAccessPolicy;
import com.microsoft.azure.storage.core.SR;

/**
 * Represents a shared access policy, which specifies the start time, expiry time, and permissions for a shared access
 * signature.
 */
public final class SharedAccessTablePolicy extends SharedAccessPolicy {
    /**
     * The permissions for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessTablePermissions> permissions;

    /**
     * Gets the permissions for a shared access signature associated with this shared access policy.
     * 
     * @return A <code>java.util.EnumSet</code> object that contains {@link SharedAccessTablePermissions} values that
     *         represents the set of shared access permissions.
     */
    public EnumSet<SharedAccessTablePermissions> getPermissions() {
        return this.permissions;
    }

    /**
     * Sets the permissions for a shared access signature associated with this shared access policy.
     * 
     * @param permissions
     *            The permissions, represented by a <code>java.util.EnumSet</code> object that contains
     *            {@link SharedAccessTablePermissions} values, to set for the shared access signature.
     */
    public void setPermissions(final EnumSet<SharedAccessTablePermissions> permissions) {
        this.permissions = permissions;
    }

    /**
     * Converts this policy's permissions to a string.
     * 
     * @return A <code>String</code> that represents the shared access permissions in the "raud" format, which is
     *         described at {@link SharedAccessTablePolicy#setPermissionsFromString(String)}.
     */
    @Override
    public String permissionsToString() {
        if (this.permissions == null) {
            return Constants.EMPTY_STRING;
        }

        // The service supports a fixed order => raud
        final StringBuilder builder = new StringBuilder();

        if (this.permissions.contains(SharedAccessTablePermissions.QUERY)) {
            builder.append("r");
        }

        if (this.permissions.contains(SharedAccessTablePermissions.ADD)) {
            builder.append("a");
        }

        if (this.permissions.contains(SharedAccessTablePermissions.UPDATE)) {
            builder.append("u");
        }

        if (this.permissions.contains(SharedAccessTablePermissions.DELETE)) {
            builder.append("d");
        }

        return builder.toString();
    }

    /**
     * Sets shared access permissions using the specified permissions string.
     * 
     * @param value
     *            A <code>String</code> that represents the shared access permissions. The string must contain one or
     *            more of the following values. Note that they must be lower case, and the order that they are specified
     *            must be in the order of "raud".
     *            <ul>
     *            <li><code>r</code>: Query access.</li>
     *            <li><code>a</code>: Add access.</li>
     *            <li><code>u</code>: Update access.</li>
     *            <li><code>d</code>: Delete access.</li>
     *            </ul>
     */
    @Override
    public void setPermissionsFromString(final String value) {
        final char[] chars = value.toCharArray();
        final EnumSet<SharedAccessTablePermissions> retSet = EnumSet.noneOf(SharedAccessTablePermissions.class);

        for (final char c : chars) {
            switch (c) {
                case 'r':
                    retSet.add(SharedAccessTablePermissions.QUERY);
                    break;
                case 'a':
                    retSet.add(SharedAccessTablePermissions.ADD);
                    break;
                case 'u':
                    retSet.add(SharedAccessTablePermissions.UPDATE);
                    break;
                case 'd':
                    retSet.add(SharedAccessTablePermissions.DELETE);
                    break;
                default:
                    throw new IllegalArgumentException(String.format(SR.ENUM_COULD_NOT_BE_PARSED, "Permissions", value));
            }
        }

        this.permissions = retSet;
    }
}
