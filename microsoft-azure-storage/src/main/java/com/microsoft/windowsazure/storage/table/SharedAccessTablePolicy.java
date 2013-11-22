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
package com.microsoft.windowsazure.storage.table;

import java.util.Date;
import java.util.EnumSet;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.core.SR;

/**
 * Represents a shared access policy, which specifies the start time, expiry time, and permissions for a shared access
 * signature.
 */
public final class SharedAccessTablePolicy {

    /**
     * Assigns shared access permissions using the specified permissions string.
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
     * 
     * @return A <code>java.util.EnumSet</code> object that contains {@link SharedAccessTablePermissions} values that
     *         represents the set of shared access permissions.
     */
    public static EnumSet<SharedAccessTablePermissions> permissionsFromString(final String value) {
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
                    throw new IllegalArgumentException(String.format(SR.PERMISSIONS_COULD_NOT_BE_PARSED, value));
            }
        }

        return retSet;
    }

    /**
     * Converts the permissions specified for the shared access policy to a string.
     * 
     * @param permissions
     *            A set of {@link SharedAccessTablePermissions} objects that represent the shared access permissions.
     * 
     * @return A <code>String</code> that represents the shared access permissions in the "raud" format, which is
     *         described at {@link SharedAccessTablePolicy#permissionsFromString(String)}.
     */
    public static String permissionsToString(final EnumSet<SharedAccessTablePermissions> permissions) {
        if (permissions == null) {
            return Constants.EMPTY_STRING;
        }

        // The service supports a fixed order => rwdl
        final StringBuilder builder = new StringBuilder();

        if (permissions.contains(SharedAccessTablePermissions.QUERY)) {
            builder.append("r");
        }

        if (permissions.contains(SharedAccessTablePermissions.ADD)) {
            builder.append("a");
        }

        if (permissions.contains(SharedAccessTablePermissions.UPDATE)) {
            builder.append("u");
        }

        if (permissions.contains(SharedAccessTablePermissions.DELETE)) {
            builder.append("d");
        }

        return builder.toString();
    }

    /**
     * The permissions for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessTablePermissions> permissions;

    /**
     * The expiry time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessExpiryTime;

    /**
     * The start time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessStartTime;

    /**
     * Creates an instance of the <code>SharedAccessTablePolicy</code> class.
     * */
    public SharedAccessTablePolicy() {
        // Empty Default Ctor
    }

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
     * Gets the expiry time for a shared access signature associated with this shared access policy.
     * 
     * @return A <code>Date</code> object that contains the shared access signature expiry time.
     */
    public Date getSharedAccessExpiryTime() {
        return this.sharedAccessExpiryTime;
    }

    /**
     * Gets the start time for a shared access signature associated with this shared access policy.
     * 
     * @return A <code>Date</code> object that contains the shared access signature start time.
     */
    public Date getSharedAccessStartTime() {
        return this.sharedAccessStartTime;
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
     * Sets the expiry time for a shared access signature associated with this shared access policy.
     * 
     * @param sharedAccessExpiryTime
     *            The expiry time to set for the shared access signature.
     */
    public void setSharedAccessExpiryTime(final Date sharedAccessExpiryTime) {
        this.sharedAccessExpiryTime = sharedAccessExpiryTime;
    }

    /**
     * Sets the start time for a shared access signature associated with this shared access policy.
     * 
     * @param sharedAccessStartTime
     *            The start time to set for the shared access signature.
     */
    public void setSharedAccessStartTime(final Date sharedAccessStartTime) {
        this.sharedAccessStartTime = sharedAccessStartTime;
    }
}
