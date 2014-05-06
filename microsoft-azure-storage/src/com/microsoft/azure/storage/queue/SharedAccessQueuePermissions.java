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

/**
 * Specifies the set of possible permissions for a shared access queue policy.
 */
public enum SharedAccessQueuePermissions {

    /**
     * No shared access granted.
     */
    NONE((byte) 0x0),

    /**
     * Permission to peek messages and get queue metadata granted.
     */
    READ((byte) 0x1),

    /**
     * Permission to add messages granted.
     */
    ADD((byte) 0x2),

    /**
     * Permissions to update messages granted.
     */
    UPDATE((byte) 0x4),

    /**
     * Permission to get and delete messages granted.
     */
    PROCESSMESSAGES((byte) 0x8);

    /**
     * Returns the enum set representing the shared access permissions for the specified byte value.
     * 
     * @param value
     *            The byte value to convert to the corresponding enum set.
     * @return A <code>java.util.EnumSet</code> object that contains the <code>SharedAccessQueuePermissions</code>
     *         values
     *         corresponding to the specified byte value.
     */
    protected static EnumSet<SharedAccessQueuePermissions> fromByte(final byte value) {
        final EnumSet<SharedAccessQueuePermissions> retSet = EnumSet.noneOf(SharedAccessQueuePermissions.class);

        if (value == READ.value) {
            retSet.add(READ);
        }

        if (value == PROCESSMESSAGES.value) {
            retSet.add(PROCESSMESSAGES);
        }
        if (value == ADD.value) {
            retSet.add(ADD);
        }
        if (value == UPDATE.value) {
            retSet.add(UPDATE);
        }

        return retSet;
    }

    /**
     * Returns the value of this enum.
     */
    private byte value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *            The value being assigned.
     */
    private SharedAccessQueuePermissions(final byte val) {
        this.value = val;
    }
}
