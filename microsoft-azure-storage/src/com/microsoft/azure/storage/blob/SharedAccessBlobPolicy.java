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
package com.microsoft.azure.storage.blob;

import java.util.EnumSet;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.SharedAccessPolicy;

/**
 * Represents a shared access policy, which specifies the start time, expiry time, and permissions for a shared access
 * signature.
 */
public final class SharedAccessBlobPolicy extends SharedAccessPolicy {
    /**
     * The permissions for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessBlobPermissions> permissions;

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
     * Converts this policy's permissions to a string.
     * 
     * @return A <code>String</code> that represents the shared access permissions in the "racwdl" format,
     *         which is described at {@link #setPermissionsFromString(String)}.
     */
    @Override
    public String permissionsToString() {
        if (this.permissions == null) {
            return Constants.EMPTY_STRING;
        }

        // The service supports a fixed order => racwdl
        final StringBuilder builder = new StringBuilder();

        if (this.permissions.contains(SharedAccessBlobPermissions.READ)) {
            builder.append("r");
        }

        if (this.permissions.contains(SharedAccessBlobPermissions.ADD)) {
            builder.append("a");
        }

        if (this.permissions.contains(SharedAccessBlobPermissions.CREATE)) {
            builder.append("c");
        }

        if (this.permissions.contains(SharedAccessBlobPermissions.WRITE)) {
            builder.append("w");
        }

        if (this.permissions.contains(SharedAccessBlobPermissions.DELETE)) {
            builder.append("d");
        }

        if (this.permissions.contains(SharedAccessBlobPermissions.LIST)) {
            builder.append("l");
        }

        return builder.toString();
    }

    /**
     * Sets shared access permissions using the specified permissions string.
     * 
     * @param value
     *            A <code>String</code> that represents the shared access permissions. The string must contain one or
     *            more of the following values. Note they must all be lowercase.
     *            <ul>
     *            <li><code>r</code>: Read access.</li>
     *            <li><code>a</code>: Add access.</li>
     *            <li><code>c</code>: Create access.</li>
     *            <li><code>w</code>: Write access.</li>
     *            <li><code>d</code>: Delete access.</li>
     *            <li><code>l</code>: List access.</li>
     *            </ul>
     */
    @Override
    public void setPermissionsFromString(final String value) {
        final EnumSet<SharedAccessBlobPermissions> initial = EnumSet.noneOf(SharedAccessBlobPermissions.class);
        for (final char c : value.toCharArray()) {
            switch (c) {
                case 'r':
                    initial.add(SharedAccessBlobPermissions.READ);
                    break;

                case 'a':
                    initial.add(SharedAccessBlobPermissions.ADD);
                    break;

                case 'c':
                    initial.add(SharedAccessBlobPermissions.CREATE);
                    break;

                case 'w':
                    initial.add(SharedAccessBlobPermissions.WRITE);
                    break;

                case 'd':
                    initial.add(SharedAccessBlobPermissions.DELETE);
                    break;

                case 'l':
                    initial.add(SharedAccessBlobPermissions.LIST);
                    break;

                default:
                    throw new IllegalArgumentException("value");
            }
        }

        this.permissions = initial;
    }
}