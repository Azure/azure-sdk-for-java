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
 * The Enum ChannelSize.
 */
public enum ChannelSize {
    /** The Small. */
    Small(0),

    /** The Medium. */
    Medium(1),

    /** The Large. */
    Large(2);

    /** The Channel state code. */
    private int channelSizeCode;

    /**
     * Instantiates a new Channel size.
     * 
     * @param ChannelStateCode
     *            the Channel state code
     */
    private ChannelSize(int ChannelSizeCode) {
        this.channelSizeCode = ChannelSizeCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return this.channelSizeCode;
    }

    /**
     * From code.
     * 
     * @param ChannelStateCode
     *            the Channel state code
     * @return the Channel state
     */
    public static ChannelSize fromCode(int ChannelStateCode) {
        switch (ChannelStateCode) {
            case 0:
                return ChannelSize.Small;
            case 1:
                return ChannelSize.Medium;
            case 2:
                return ChannelSize.Large;
            default:
                throw new InvalidParameterException("ChannelStateCode");
        }
    }
}
