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
public final class SharedAccessBlobPolicy {

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
     * @return A <code>java.util.EnumSet</code> object that contains {@link SharedAccessBlobPermissions} values that
     *         represents the set of shared access permissions.
     */
    public static EnumSet<SharedAccessBlobPermissions> permissionsFromString(final String value) {
        final char[] chars = value.toCharArray();
        final EnumSet<SharedAccessBlobPermissions> retSet = EnumSet.noneOf(SharedAccessBlobPermissions.class);

        for (final char c : chars) {
            switch (c) {
                case 'r':
                    retSet.add(SharedAccessBlobPermissions.READ);
                    break;
                case 'w':
                    retSet.add(SharedAccessBlobPermissions.WRITE);
                    break;
                case 'd':
                    retSet.add(SharedAccessBlobPermissions.DELETE);
                    break;
                case 'l':
                    retSet.add(SharedAccessBlobPermissions.LIST);
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
     *            A {@link SharedAccessBlobPermissions} object that represents the shared access permissions.
     * 
     * @return A <code>String</code> that represents the shared access permissions in the "rwdl" format, which is
     *         described at {@link SharedAccessBlobPolicy#permissionsFromString}.
     */
    public static String permissionsToString(final EnumSet<SharedAccessBlobPermissions> permissions) {
        if (permissions == null) {
            return Constants.EMPTY_STRING;
        }

        // The service supports a fixed order => rwdl
        final StringBuilder builder = new StringBuilder();

        if (permissions.contains(SharedAccessBlobPermissions.READ)) {
            builder.append("r");
        }

        if (permissions.contains(SharedAccessBlobPermissions.WRITE)) {
            builder.append("w");
        }

        if (permissions.contains(SharedAccessBlobPermissions.DELETE)) {
            builder.append("d");
        }

        if (permissions.contains(SharedAccessBlobPermissions.LIST)) {
            builder.append("l");
        }

        return builder.toString();
    }

    /**
     * The permissions for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessBlobPermissions> permissions;

    /**
     * The expiry time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessExpiryTime;

    /**
     * The start time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessStartTime;

    /**
     * Creates an instance of the <code>SharedAccessBlobPolicy</code> class.
     * */
    public SharedAccessBlobPolicy() {
        // Empty Default Ctor
    }

    /**
     * Gets the permissions for a shared access signature associated with this shared access policy.
     * 
     * @return A <code>java.util.EnumSet</code> object that contains {@link SharedAccessBlobPermissions} values that
     *         represents the set of shared access permissions.
     */
    public EnumSet<SharedAccessBlobPermissions> getPermissions() {
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
     *            {@link SharedAccessBlobPermissions} values, to set for the shared access signature.
     */
    public void setPermissions(final EnumSet<SharedAccessBlobPermissions> permissions) {
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
