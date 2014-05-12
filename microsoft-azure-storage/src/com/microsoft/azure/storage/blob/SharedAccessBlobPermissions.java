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

/**
 * Specifies the set of possible permissions for a shared access policy.
 */
public enum SharedAccessBlobPermissions {
    /**
     * Specifies Read access granted.
     */
    READ((byte) 0x1),

    /**
     * Specifies Write access granted.
     */
    WRITE((byte) 0x2),

    /**
     * Specifies Delete access granted for blobs.
     */
    DELETE((byte) 0x4),

    /**
     * Specifies List access granted.
     */
    LIST((byte) 0x8);

    /**
     * Returns the enum set representing the shared access permissions for the specified byte value.
     * 
     * @param value
     *            The byte value to convert to the corresponding enum set.
     * @return A <code>java.util.EnumSet</code> object that contains the <code>SharedAccessBlobPermissions</code> values
     *         corresponding to the specified byte value.
     */
    protected static EnumSet<SharedAccessBlobPermissions> fromByte(final byte value) {
        final EnumSet<SharedAccessBlobPermissions> retSet = EnumSet.noneOf(SharedAccessBlobPermissions.class);

        if (value == READ.value) {
            retSet.add(READ);
        }

        if (value == WRITE.value) {
            retSet.add(WRITE);
        }
        if (value == DELETE.value) {
            retSet.add(DELETE);
        }
        if (value == LIST.value) {
            retSet.add(LIST);
        }

        return retSet;
    }

    /**
     * Represents the value of this enum.
     */
    private byte value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *        A <code>byte</code> which specifies the value being assigned.
     */
    private SharedAccessBlobPermissions(final byte val) {
        this.value = val;
    }
}
