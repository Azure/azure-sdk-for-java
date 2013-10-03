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

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class SecuritySettings.
 */
public class SecuritySettings {

    /** The ip v4 white list. */
    private List<Ipv4> ipV4AllowList = null;

    /** The akamai g20 authentication. */
    private List<G20Key> akamaiG20Authentication = null;

    /**
     * Gets the ip v4 white list.
     * 
     * @return the ip v4 white list
     */
    @JsonProperty("IPv4AllowList")
    public List<Ipv4> getIpV4AllowList() {
        return this.ipV4AllowList;
    }

    /**
     * Sets the ip v4 allow list.
     * 
     * @param ipV4AllowList
     *            the ip v4 allow list
     * @return the security settings
     */
    @JsonProperty("IPv4AllowList")
    public SecuritySettings setIpV4AllowList(List<Ipv4> ipV4AllowList) {
        this.ipV4AllowList = ipV4AllowList;
        return this;
    }

    /**
     * Gets the akamai g20 authentication.
     * 
     * @return the akamai g20 authentication
     */
    @JsonProperty("AkamaiG20Authentication")
    public List<G20Key> getAkamaiG20Authentication() {
        return this.akamaiG20Authentication;
    }

    /**
     * Sets the akamai g20 authentication.
     * 
     * @param akamaiG20Authentication
     *            the akamai g20 authentication
     * @return the security settings
     */
    public SecuritySettings setAkamaiG20Authentication(List<G20Key> akamaiG20Authentication) {
        this.akamaiG20Authentication = akamaiG20Authentication;
        return this;
    }
}
