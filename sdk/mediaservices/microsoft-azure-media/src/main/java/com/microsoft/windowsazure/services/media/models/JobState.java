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
 * The Enum JobState.
 */
public enum JobState {
    /** The Queued. */
    Queued(0),

    /** The Scheduled. */
    Scheduled(1),

    /** The Processing. */
    Processing(2),

    /** The Finished. */
    Finished(3),

    /** The Error. */
    Error(4),

    /** The Canceled. */
    Canceled(5),

    /** The Canceling. */
    Canceling(6);

    /** The job state code. */
    private int jobStateCode;

    /**
     * Instantiates a new job state.
     * 
     * @param jobStateCode
     *            the job state code
     */
    private JobState(int jobStateCode) {
        this.jobStateCode = jobStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return this.jobStateCode;
    }

    /**
     * From code.
     * 
     * @param jobStateCode
     *            the job state code
     * @return the job state
     */
    public static JobState fromCode(int jobStateCode) {
        switch (jobStateCode) {
        case 0:
            return JobState.Queued;
        case 1:
            return JobState.Scheduled;
        case 2:
            return JobState.Processing;
        case 3:
            return JobState.Finished;
        case 4:
            return JobState.Error;
        case 5:
            return JobState.Canceled;
        case 6:
            return JobState.Canceling;
        default:
            throw new InvalidParameterException("jobStateCode");
        }
    }
}
