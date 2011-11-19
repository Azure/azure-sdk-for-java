/**
 * 
 */
package com.microsoft.windowsazure.services.blob.client;

import java.util.Locale;

import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Specifies the type of a blob.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum BlobType {
    /**
     * Specifies the blob type is not specified.
     */
    UNSPECIFIED,

    /**
     * Specifies the blob is a block blob.
     */
    BLOCK_BLOB,

    /**
     * Specifies the blob is a page blob.
     */
    PAGE_BLOB;

    /**
     * Returns the enum value representing the blob type for the specified string.
     * 
     * @param typeString
     *            A <code>String</code> that represents a blob type, such as "blockblob" or "pageblob".
     * 
     * @return A <code>BlobType</code> value corresponding to the string specified by <code>typeString</code>.
     */
    public static BlobType parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return UNSPECIFIED;
        } else if ("blockblob".equals(typeString.toLowerCase(Locale.US))) {
            return BLOCK_BLOB;
        } else if ("pageblob".equals(typeString.toLowerCase(Locale.US))) {
            return PAGE_BLOB;
        } else {
            return UNSPECIFIED;
        }
    }
}
