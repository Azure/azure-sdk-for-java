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

import java.util.Locale;

import com.microsoft.azure.storage.core.Utility;

/**
 * Specifies the type of a blob.
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
    PAGE_BLOB,
    
    /**
     * Specifies the blob is an append blob.
     */
    APPEND_BLOB;

    /**
     * Returns the enum value representing the blob type for the specified string.
     * 
     * @param typeString
     *            A <code>String</code> that represents a blob type, such as "blockblob" or "pageblob".
     * 
     * @return A <code>BlobType</code> value corresponding to the string specified by <code>typeString</code>.
     */
    protected static BlobType parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return UNSPECIFIED;
        }
        else if ("blockblob".equals(typeString.toLowerCase(Locale.US))) {
            return BLOCK_BLOB;
        }
        else if ("pageblob".equals(typeString.toLowerCase(Locale.US))) {
            return PAGE_BLOB;
        }
        else if ("appendblob".equals(typeString.toLowerCase(Locale.US))) {
            return APPEND_BLOB;
        }
        else {
            return UNSPECIFIED;
        }
    }
}
