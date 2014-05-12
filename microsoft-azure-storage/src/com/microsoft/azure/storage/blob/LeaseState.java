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
 * The lease state of a resource.
 */
public enum LeaseState {
    /**
     * The lease state is not specified.
     */
    UNSPECIFIED,

    /**
     * The lease is in the Available state.
     */
    AVAILABLE,

    /**
     * The lease is in the Leased state.
     */
    LEASED,

    /**
     * The lease is in the Expired state.
     */
    EXPIRED,

    /**
     * The lease is in the Breaking state.
     */
    BREAKING,

    /**
     * The lease is in the Broken state.
     */
    BROKEN;

    /**
     * Parses a lease state from the given string.
     * 
     * @param typeString
     *        A <code>String</code> which contains the lease state to parse.
     * 
     * @return A <code>LeaseStatus</code> value that represents the lease status.
     */
    protected static LeaseState parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return UNSPECIFIED;
        }
        else if ("available".equals(typeString.toLowerCase(Locale.US))) {
            return AVAILABLE;
        }
        else if ("leased".equals(typeString.toLowerCase(Locale.US))) {
            return LEASED;
        }
        else if ("expired".equals(typeString.toLowerCase(Locale.US))) {
            return EXPIRED;
        }
        else if ("breaking".equals(typeString.toLowerCase(Locale.US))) {
            return BREAKING;
        }
        else if ("broken".equals(typeString.toLowerCase(Locale.US))) {
            return BROKEN;
        }
        else {
            return UNSPECIFIED;
        }
    }
}
