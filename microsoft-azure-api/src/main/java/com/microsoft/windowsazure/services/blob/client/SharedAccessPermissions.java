package com.microsoft.windowsazure.services.blob.client;

import java.util.EnumSet;

/**
 * Specifies the set of possible permissions for a shared access policy.
 */
public enum SharedAccessPermissions {
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
     * @return A <code>java.util.EnumSet</code> object that contains the <code>SharedAccessPermissions</code> values
     *         corresponding to the specified byte value.
     */
    protected static EnumSet<SharedAccessPermissions> fromByte(final byte value) {
        final EnumSet<SharedAccessPermissions> retSet = EnumSet.noneOf(SharedAccessPermissions.class);

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
     * Returns the value of this enum.
     */
    private byte value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *            The value being assigned.
     */
    SharedAccessPermissions(final byte val) {
        this.value = val;
    }
}
