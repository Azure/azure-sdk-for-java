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
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import com.microsoft.windowsazure.services.core.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. Describes actions that can be performed on a lease.
 */
public enum LeaseAction {

    /**
     * Acquire the lease.
     */
    ACQUIRE,

    /**
     * Renew the lease.
     */
    RENEW,

    /**
     * Release the lease.
     */
    RELEASE,

    /**
     * Break the lease.
     */
    BREAK,

    /**
     * Change the lease.
     */
    CHANGE;

    @Override
    public String toString() {
        switch (this) {
            case ACQUIRE:
                return "Acquire";
            case RENEW:
                return "Renew";
            case RELEASE:
                return "Release";
            case BREAK:
                return "Break";
            case CHANGE:
                return "Change";
            default:
                // Wont Happen, all possible values covered above.
                return Constants.EMPTY_STRING;
        }
    }
}
