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
 * Enum describing options for creating tasks
 * 
 */
public enum TaskOption {

    /**
     * None
     */
    None(0),

    /**
     * Encrypt task configuration
     */
    ProtectedConfiguration(1);

    private int code;

    private TaskOption(int code) {
        this.code = code;
    }

    /**
     * Get integer code corresponding to enum value
     * 
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Return enum value corresponding to integer code
     * 
     * @param code
     *            the code
     * @return the enum value
     */
    public static TaskOption fromCode(int code) {
        switch (code) {
        case 0:
            return None;
        case 1:
            return ProtectedConfiguration;

        default:
            throw new InvalidParameterException("code");
        }
    }
}
