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
 * The Enum ChannelState.
 */
public enum ChannelState {
    /** The Stopped. */
    Stopped(0),

    /** The Starting. */
    Starting(1),

    /** The Running. */
    Running(2),

    /** The Stopping. */
    Stopping(3),

    /** The Deleting. */
    Deleting(4);

    /** The Channel state code. */
    private int channelStateCode;

    /**
     * Instantiates a new Channel state.
     * 
     * @param ChannelStateCode
     *            the Channel state code
     */
    private ChannelState(int ChannelStateCode) {
        this.channelStateCode = ChannelStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return this.channelStateCode;
    }

    /**
     * From code.
     * 
     * @param ChannelStateCode
     *            the Channel state code
     * @return the Channel state
     */
    public static ChannelState fromCode(int ChannelStateCode) {
        switch (ChannelStateCode) {
            case 0:
                return ChannelState.Stopped;
            case 1:
                return ChannelState.Starting;
            case 2:
                return ChannelState.Running;
            case 3:
                return ChannelState.Stopping;
            case 4:
                return ChannelState.Deleting;
            default:
                throw new InvalidParameterException("ChannelStateCode");
        }
    }
}
