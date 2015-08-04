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
package com.microsoft.azure.storage.queue;

import java.util.EnumSet;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.SharedAccessPolicy;

/**
 * Represents a shared access policy, which specifies the start time, expiry time, and permissions for a shared access
 * signature.
 */
public final class SharedAccessQueuePolicy extends SharedAccessPolicy {
    /**
     * The permissions for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessQueuePermissions> permissions;

    /**
     * Gets the permissions for a shared access signature associated with this shared access policy.
     * 
     * @return A <code>java.util.EnumSet</code> object that contains {@link SharedAccessQueuePermissions} values that
     *         represents the set of shared access permissions.
     */
    public EnumSet<SharedAccessQueuePermissions> getPermissions() {
        return this.permissions;
    }

    /**
     * Sets the permissions for a shared access signature associated with this shared access policy.
     * 
     * @param permissions
     *            The permissions, represented by a <code>java.util.EnumSet</code> object that contains
     *            {@link SharedAccessQueuePermissions} values, to set for the shared access signature.
     */
    public void setPermissions(final EnumSet<SharedAccessQueuePermissions> permissions) {
        this.permissions = permissions;
    }

    /**
     * Converts this policy's permissions to a string.
     * 
     * @return A <code>String</code> that represents the shared access permissions in the "raup" format, which is
     *         described at {@link SharedAccessQueuePolicy#setPermissionsFromString(String)}.
     */
    @Override
    public String permissionsToString() {
        if (this.permissions == null) {
            return Constants.EMPTY_STRING;
        }

        // The service supports a fixed order => raup
        final StringBuilder builder = new StringBuilder();

        if (this.permissions.contains(SharedAccessQueuePermissions.READ)) {
            builder.append("r");
        }

        if (this.permissions.contains(SharedAccessQueuePermissions.ADD)) {
            builder.append("a");
        }

        if (this.permissions.contains(SharedAccessQueuePermissions.UPDATE)) {
            builder.append("u");
        }

        if (this.permissions.contains(SharedAccessQueuePermissions.PROCESSMESSAGES)) {
            builder.append("p");
        }

        return builder.toString();
    }

    /**
     * Sets shared access permissions using the specified permissions string.
     * 
     * @param value
     *            A <code>String</code> that represents the shared access permissions. The string must contain one or
     *            more of the following values. Note that they must be lower case, and the order that they are specified
     *            must be in the order of "raup".
     *            <ul>
     *            <li><code>d</code>: Read access.</li>
     *            <li><code>l</code>: Add access.</li>
     *            <li><code>r</code>: Update access.</li>
     *            <li><code>w</code>: ProcessMessages access.</li>
     *            </ul>
     */
    @Override
    public void setPermissionsFromString(final String value) {
        final char[] chars = value.toCharArray();
        final EnumSet<SharedAccessQueuePermissions> retSet = EnumSet.noneOf(SharedAccessQueuePermissions.class);

        for (final char c : chars) {
            switch (c) {
                case 'r':
                    retSet.add(SharedAccessQueuePermissions.READ);
                    break;
                case 'a':
                    retSet.add(SharedAccessQueuePermissions.ADD);
                    break;
                case 'u':
                    retSet.add(SharedAccessQueuePermissions.UPDATE);
                    break;
                case 'p':
                    retSet.add(SharedAccessQueuePermissions.PROCESSMESSAGES);
                    break;
                default:
                    throw new IllegalArgumentException("value");
            }
        }

        this.permissions = retSet;
    }
}
