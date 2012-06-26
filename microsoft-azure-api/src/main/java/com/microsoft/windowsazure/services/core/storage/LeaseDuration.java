/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.storage;

import java.util.Locale;

import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * The lease duration of a resource.
 */
public enum LeaseDuration {
    /**
     * The lease duration is not specified.
     */
    UNSPECIFIED,

    /**
     * The lease duration is finite.
     */
    FIXED,

    /**
     * The lease duration is infinite.
     */
    INFINITE;

    /**
     * Parses a lease duration from the given string.
     * 
     * @param typeString
     *            A <code>String</code> that represents the string to parse.
     * 
     * @return A <code>LeaseStatus</code> value that represents the lease status.
     */
    public static LeaseDuration parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return UNSPECIFIED;
        }
        else if ("fixed".equals(typeString.toLowerCase(Locale.US))) {
            return FIXED;
        }
        else if ("infinite".equals(typeString.toLowerCase(Locale.US))) {
            return INFINITE;
        }
        else {
            return UNSPECIFIED;
        }
    }
}
