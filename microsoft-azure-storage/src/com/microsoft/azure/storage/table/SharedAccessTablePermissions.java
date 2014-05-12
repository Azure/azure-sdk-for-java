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

/**
 * Specifies the set of possible permissions for a shared access table policy.
 */
public enum SharedAccessTablePermissions {

    /**
     * No shared access granted.
     */
    NONE((byte) 0x0),

    /**
     * Permission to query entities granted.
     */
    QUERY((byte) 0x1),

    /**
     * Permission to add entities granted.
     */
    ADD((byte) 0x2),

    /**
     * Permission to modify entities granted.
     */
    UPDATE((byte) 0x4),

    /**
     * Permission to delete entities granted.
     */
    DELETE((byte) 0x8);

    /**
     * Returns the enum set representing the shared access permissions for the specified byte value.
     * 
     * @param value
     *        A <code>byte</code> which represents the value to convert to the corresponding enum set.
     *            
     * @return A <code>java.util.EnumSet</code> object that contains the <code>SharedAccessTablePermissions</code> values
     *         corresponding to the specified byte value.
     */
    protected static EnumSet<SharedAccessTablePermissions> fromByte(final byte value) {
        final EnumSet<SharedAccessTablePermissions> retSet = EnumSet.noneOf(SharedAccessTablePermissions.class);

        if (value == QUERY.value) {
            retSet.add(QUERY);
        }

        if (value == ADD.value) {
            retSet.add(ADD);
        }
        if (value == UPDATE.value) {
            retSet.add(UPDATE);
        }
        if (value == DELETE.value) {
            retSet.add(DELETE);
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
     *        A <code>byte</code> which represents the value being assigned.
     */
    SharedAccessTablePermissions(final byte val) {
        this.value = val;
    }
}
