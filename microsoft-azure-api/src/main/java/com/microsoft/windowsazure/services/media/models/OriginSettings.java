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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The Class OriginSettings.
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class OriginSettings {

    /** The playback. */
    private PlaybackEndPointSettings playback;

    /**
     * Gets the playback.
     * 
     * @return the playback
     */
    @JsonProperty("Playback")
    public PlaybackEndPointSettings getPlayback() {
        return this.playback;
    }

    /**
     * Sets the playback.
     * 
     * @param playback
     *            the playback
     * @return the origin settings
     */
    @JsonProperty("Playback")
    public OriginSettings setPlayback(PlaybackEndPointSettings playback) {
        this.playback = playback;
        return this;
    }

}
