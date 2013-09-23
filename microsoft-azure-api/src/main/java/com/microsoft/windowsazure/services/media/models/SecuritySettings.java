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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class SecuritySettings.
 */
public class SecuritySettings {

    /** The ip v4 white list. */
    private final List<Ipv4> ipV4WhiteList = new ArrayList<Ipv4>();

    /** The akamai g20 authentication. */
    private final List<G20Key> akamaiG20Authentication = new ArrayList<G20Key>();

    /**
     * Gets the ip v4 white list.
     * 
     * @return the ip v4 white list
     */
    public List<Ipv4> getIpV4WhiteList() {
        return this.ipV4WhiteList;
    }

    /**
     * Gets the akamai g20 authentication.
     * 
     * @return the akamai g20 authentication
     */
    public List<G20Key> getAkamaiG20Authentication() {
        return this.akamaiG20Authentication;
    }
}
