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
 * Specifies the lease status of a blob.
 * <p>
 * You can check the lease status of a blob to determine whether it currently has an active lease (locked for
 * exclusive-write access), or whether it is available for exclusive-write access.
 */
public enum LeaseStatus {
    /**
     * Specifies the lease status is not specified.
     */
    UNSPECIFIED,

    /**
     * Specifies the blob is locked for exclusive-write access.
     */
    LOCKED,

    /**
     * Specifies the blob is available to be locked for exclusive-write access.
     */
    UNLOCKED;

    /**
     * Parses a lease status from the given string.
     * 
     * @param typeString
     *        A <code>String</code> which contains the lease status to parse.
     * 
     * @return A <code>LeaseStatus</code> value that represents the lease status.
     */
    protected static LeaseStatus parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return UNSPECIFIED;
        }
        else if ("unlocked".equals(typeString.toLowerCase(Locale.US))) {
            return UNLOCKED;
        }
        else if ("locked".equals(typeString.toLowerCase(Locale.US))) {
            return LOCKED;
        }
        else {
            return UNSPECIFIED;
        }
    }
}
