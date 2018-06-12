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
 * Enum defining the state of various tasks.
 */
public enum TaskState {

    /** No state. */
    None(0),

    /** Active. */
    Active(1),

    /** Running. */
    Running(2),

    /** Completed. */
    Completed(3),

    /** Error. */
    Error(4),

    /** The Canceled. */
    Canceled(5),

    /** The Canceling. */
    Canceling(6);

    /** The code. */
    private int code;

    /**
     * Instantiates a new task state.
     * 
     * @param code
     *            the code
     */
    private TaskState(int code) {
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
    public static TaskState fromCode(int code) {
        switch (code) {
        case 0:
            return None;
        case 1:
            return Active;
        case 2:
            return Running;
        case 3:
            return Completed;
        case 4:
            return Error;
        case 5:
            return Canceled;
        case 6:
            return Canceling;
        default:
            throw new InvalidParameterException("code");
        }
    }
}
