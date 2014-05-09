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

package com.microsoft.windowsazure.services.media.models;

import java.security.InvalidParameterException;

/**
 * Enum defining the type of the end point.
 */
public enum EndPointType {

    /** Azure Queue. */
    AzureQueue(1);

    /** The code. */
    private int code;

    /**
     * Instantiates a new end point type.
     * 
     * @param code
     *            the code
     */
    private EndPointType(int code) {
        this.code = code;
    }

    /**
     * Get integer code corresponding to enum value.
     * 
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Convert code into enum value.
     * 
     * @param code
     *            the code
     * @return the corresponding enum value
     */
    public static EndPointType fromCode(int code) {
        switch (code) {
        case 1:
            return AzureQueue;
        default:
            throw new InvalidParameterException("code");
        }
    }
}
