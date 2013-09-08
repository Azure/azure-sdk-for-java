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

public class SecuritySettings {
    private List<Ipv4> ipV4WhiteList;
    private List<G20Key> akamaiG20Authentication;

    public List<Ipv4> getIpV4WhiteList() {
        return this.ipV4WhiteList;
    }

    public List<G20Key> getAkamaiG20Authentication() {
        return this.akamaiG20Authentication;
    }
}
