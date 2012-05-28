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
package com.microsoft.windowsazure.services.blob.client;

import java.util.Date;
import java.util.EnumSet;

import com.microsoft.windowsazure.services.core.storage.Constants;

/**
 * Represents a shared access policy, which specifies the start time, expiry time, and permissions for a shared access
 * signature.
 */
public final class SharedAccessPolicy {

    /**
     * Assigns shared access permissions using the specified permissions string.
     * 
     * @param value
     *            A <code>String</code> that represents the shared access permissions. The string must contain one or
     *            more of the following values. Note they must be lowercase, and the order that they are specified must
     *            be in the order of "rwdl".
     *            <ul>
     *            <li><code>d</code>: Delete access.</li>
     *            <li><code>l</code>: List access.</li>
     *            <li><code>r</code>: Read access.</li>
     *            <li><code>w</code>: Write access.</li>
     *            </ul>
     * 
     * @return A <code>java.util.EnumSet</code> object that contains {@link SharedAccessPermissions} values that
     *         represents the set of shared access permissions.
     */
    public static EnumSet<SharedAccessPermissions> permissionsFromString(final String value) {
        final char[] chars = value.toCharArray();
        final EnumSet<SharedAccessPermissions> retSet = EnumSet.noneOf(SharedAccessPermissions.class);

        for (final char c : chars) {
            switch (c) {
                case 'r':
                    retSet.add(SharedAccessPermissions.READ);
                    break;
                case 'w':
                    retSet.add(SharedAccessPermissions.WRITE);
                    break;
                case 'd':
                    retSet.add(SharedAccessPermissions.DELETE);
                    break;
                case 'l':
                    retSet.add(SharedAccessPermissions.LIST);
                    break;
                default:
                    throw new IllegalArgumentException("value");
            }
        }

        return retSet;
    }

    /**
     * Converts the permissions specified for the shared access policy to a string.
     * 
     * @param permissions
     *            A {@link SharedAccessPermissions} object that represents the shared access permissions.
     * 
     * @return A <code>String</code> that represents the shared access permissions in the "rwdl" format, which is
     *         described at {@link SharedAccessPolicy#permissionsFromString}.
     */
    public static String permissionsToString(final EnumSet<SharedAccessPermissions> permissions) {
        if (permissions == null) {
            return Constants.EMPTY_STRING;
        }

        // The service supports a fixed order => rwdl
        final StringBuilder builder = new StringBuilder();

        if (permissions.contains(SharedAccessPermissions.READ)) {
            builder.append("r");
        }

        if (permissions.contains(SharedAccessPermissions.WRITE)) {
            builder.append("w");
        }

        if (permissions.contains(SharedAccessPermissions.DELETE)) {
            builder.append("d");
        }

        if (permissions.contains(SharedAccessPermissions.LIST)) {
            builder.append("l");
        }

        return builder.toString();
    }

    /**
     * The permissions for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessPermissions> permissions;

    /**
     * The expiry time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessExpiryTime;

    /**
     * The start time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessStartTime;

    /**
     * Creates an instance of the <code>SharedAccessPolicy</code> class.
     * */
    public SharedAccessPolicy() {
        // Empty Default Ctor
    }

    /**
     * @return the permissions
     */
    public EnumSet<SharedAccessPermissions> getPermissions() {
        return this.permissions;
    }

    /**
     * @return the sharedAccessExpiryTime
     */
    public Date getSharedAccessExpiryTime() {
        return this.sharedAccessExpiryTime;
    }

    /**
     * @return the sharedAccessStartTime
     */
    public Date getSharedAccessStartTime() {
        return this.sharedAccessStartTime;
    }

    /**
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions(final EnumSet<SharedAccessPermissions> permissions) {
        this.permissions = permissions;
    }

    /**
     * @param sharedAccessExpiryTime
     *            the sharedAccessExpiryTime to set
     */
    public void setSharedAccessExpiryTime(final Date sharedAccessExpiryTime) {
        this.sharedAccessExpiryTime = sharedAccessExpiryTime;
    }

    /**
     * @param sharedAccessStartTime
     *            the sharedAccessStartTime to set
     */
    public void setSharedAccessStartTime(final Date sharedAccessStartTime) {
        this.sharedAccessStartTime = sharedAccessStartTime;
    }
}
