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

import java.util.Calendar;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class PlaybackEndPointSettings.
 */
public class PlaybackEndPointSettings {

    /** The max cache age. */
    private Calendar maxCacheAge;

    /** The security. */
    private SecuritySettings security;

    /**
     * Gets the max cache age.
     * 
     * @return the max cache age
     */
    @JsonProperty("MaxCacheAge")
    public Calendar getMaxCacheAge() {
        return maxCacheAge;
    }

    /**
     * Sets the max cache age.
     * 
     * @param maxCacheAge
     *            the new max cache age
     */
    @JsonProperty("MaxCacheAge")
    public PlaybackEndPointSettings setMaxCacheAge(Calendar maxCacheAge) {
        this.maxCacheAge = maxCacheAge;
        return this;
    }

    /**
     * Gets the security.
     * 
     * @return the security
     */
    @JsonProperty("Security")
    public SecuritySettings getSecurity() {
        return this.security;
    }

    /**
     * Sets the security.
     * 
     * @param security
     *            the security
     * @return the preview end point settings
     */
    @JsonProperty("Security")
    public PlaybackEndPointSettings setSecurity(SecuritySettings security) {
        this.security = security;
        return this;
    }

}
