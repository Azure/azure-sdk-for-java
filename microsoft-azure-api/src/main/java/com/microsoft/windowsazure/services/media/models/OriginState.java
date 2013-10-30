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
 * The Enum OriginState.
 */
public enum OriginState {
    /** The Stopped. */
    Stopped(0),

    /** The Starting. */
    Starting(1),

    /** The Running. */
    Running(2),

    /** The Stopping. */
    Stopping(3),

    /** The Scaling. */
    Scaling(4);

    /** The Origin state code. */
    private int originStateCode;

    /**
     * Instantiates a new origin state.
     * 
     * @param originStateCode
     *            the origin state code
     */
    private OriginState(int originStateCode) {
        this.originStateCode = originStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return this.originStateCode;
    }

    /**
     * From code.
     * 
     * @param originStateCode
     *            the Origin state code
     * @return the Origin state
     */
    public static OriginState fromCode(int originStateCode) {
        switch (originStateCode) {
            case 0:
                return OriginState.Stopped;
            case 1:
                return OriginState.Starting;
            case 2:
                return OriginState.Running;
            case 3:
                return OriginState.Stopping;
            case 4:
                return OriginState.Scaling;
            default:
                throw new InvalidParameterException("originStateCode");
        }
    }
}
