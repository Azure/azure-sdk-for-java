/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

import java.util.Locale;

import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Specifies the lease status of a blob.
 * <p>
 * You can check the lease status of a blob to determine whether it currently has an active lease (locked for
 * exclusive-write access), or whether it is available for exclusive-write access.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum LeaseStatus {
    /**
     * Specifies the blob is locked for exclusive-write access.
     */
    LOCKED,

    /**
     * Specifies the blob is available to be locked for exclusive-write access.
     */
    UNLOCKED,

    /**
     * Specifies the lease status is not specified.
     */
    UNSPECIFIED;

    /**
     * Parses a lease status from the given string.
     * 
     * @param typeString
     *            A <code>String</code> that represents the string to parse.
     * 
     * @return A <code>LeaseStatus</code> value that represents the lease status.
     */
    public static LeaseStatus parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return UNSPECIFIED;
        } else if ("unlocked".equals(typeString.toLowerCase(Locale.US))) {
            return UNLOCKED;
        } else if ("locked".equals(typeString.toLowerCase(Locale.US))) {
            return LOCKED;
        } else {
            return UNSPECIFIED;
        }
    }
}
