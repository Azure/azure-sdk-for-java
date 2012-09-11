/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

import java.util.HashMap;


/**
 * Represents the permissions for a table.
 */

public final class TablePermissions {

    /**
     * Gets the set of shared access policies for the table.
     */
    private HashMap<String, SharedAccessTablePolicy> sharedAccessPolicies;

    /**
     * Creates an instance of the <code>TablePermissions</code> class.
     */
    public TablePermissions() {
        this.sharedAccessPolicies = new HashMap<String, SharedAccessTablePolicy>();
    }

    /**
     * Returns the set of shared access policies for the table.
     * 
     * @return A <code>HashMap</code> object of {@link SharedAccessTablePolicy} objects that represent the set of shared
     *         access policies for the table.
     */
    public HashMap<String, SharedAccessTablePolicy> getSharedAccessPolicies() {
        return this.sharedAccessPolicies;
    }

    /**
     * Sets the set of shared access policies for the table.
     * 
     * @param sharedAccessPolicies
     *            The set of shared access policies to set for the table, represented by a <code>HashMap</code> object of
     *            {@link SharedAccessTablePolicy} objects.
     */
    public void setSharedAccessPolicies(final HashMap<String, SharedAccessTablePolicy> sharedAccessPolicies) {
        this.sharedAccessPolicies = sharedAccessPolicies;
    }
}