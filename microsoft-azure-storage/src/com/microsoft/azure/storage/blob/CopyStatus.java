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
 * Represents the status of a copy blob operation.
 */
public enum CopyStatus {
    /**
     * The copy status is not specified.
     */
    UNSPECIFIED,

    /**
     * The copy status is invalid.
     */
    INVALID,

    /**
     * The copy operation is pending.
     */
    PENDING,

    /**
     * The copy operation succeeded.
     */
    SUCCESS,

    /**
     * The copy operation has been aborted.
     */
    ABORTED,

    /**
     * The copy operation encountered an error.
     */
    FAILED;

    /**
     * Parses a copy status from the given string.
     * 
     * @param typeString
     *            A <code>String</code> that represents the string to parse.
     * 
     * @return A <code>CopyStatus</code> value that represents the copy status.
     */
    static CopyStatus parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return UNSPECIFIED;
        }
        else if ("invalid".equals(typeString.toLowerCase(Locale.US))) {
            return INVALID;
        }
        else if ("pending".equals(typeString.toLowerCase(Locale.US))) {
            return PENDING;
        }
        else if ("success".equals(typeString.toLowerCase(Locale.US))) {
            return SUCCESS;
        }
        else if ("aborted".equals(typeString.toLowerCase(Locale.US))) {
            return ABORTED;
        }
        else if ("failed".equals(typeString.toLowerCase(Locale.US))) {
            return FAILED;
        }
        else {
            return UNSPECIFIED;
        }
    }
}