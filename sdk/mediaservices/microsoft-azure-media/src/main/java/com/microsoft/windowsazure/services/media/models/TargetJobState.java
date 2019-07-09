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
 * The Enum TargetJobState.
 */
public enum TargetJobState {
    /** None. */
    None(0),

    /** FinalStatesOnly. */
    FinalStatesOnly(1),

    /** All. */
    All(2);

    /** The target job state code. */
    private int targetJobStateCode;

    /**
     * Instantiates a new job state.
     * 
     * @param targetJobStateCode
     *            the job state code
     */
    private TargetJobState(int targetJobStateCode) {
        this.targetJobStateCode = targetJobStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return this.targetJobStateCode;
    }

    /**
     * From code.
     * 
     * @param targetJobStateCode
     *            the target job state code
     * @return the job state
     */
    public static TargetJobState fromCode(int targetJobStateCode) {
        switch (targetJobStateCode) {
        case 0:
            return TargetJobState.None;
        case 1:
            return TargetJobState.FinalStatesOnly;
        case 2:
            return TargetJobState.All;
        default:
            throw new InvalidParameterException("targetJobStateCode");
        }
    }
}
