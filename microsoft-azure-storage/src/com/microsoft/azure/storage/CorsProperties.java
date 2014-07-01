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

package com.microsoft.azure.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the service properties pertaining to CORS.
 */
public final class CorsProperties {

    /**
     * The CORS rules associated with this set of CORS properties. The order of the list corresponds to precedence of
     * rules.
     * 
     * Limited to 5. The size of all the rules per service should not exceed 2KB of characters.
     */
    private List<CorsRule> corsRules = new ArrayList<CorsRule>();

    /**
     * @return the corsRules
     */
    public List<CorsRule> getCorsRules() {
        return this.corsRules;
    }

    /**
     * Limited to 5. The size of all the rules per service should not exceed 2KB of characters.
     * 
     * @param corsRules
     */
    protected void setCorsRules(List<CorsRule> corsRules) {
        this.corsRules = corsRules;
    }

}
