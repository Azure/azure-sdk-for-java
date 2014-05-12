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

import java.util.HashMap;

/**
 * Represents the permissions for a shared access policy.
 */
public abstract class Permissions<T extends SharedAccessPolicy> {
    /**
     * Represents the set of shared access policies.
     */
    private HashMap<String, T> sharedAccessPolicies;

    /**
     * Creates an instance of the <code>Permissions</code> class.
     */
    public Permissions() {
        this.sharedAccessPolicies = new HashMap<String, T>();
    }

    /**
     * Returns the set of shared access policies.
     * 
     * @return A <code>java.util.HashMap</code> object of {@link SharedAccessPolicy} objects
     *         which represent the set of shared access policies.
     */
    public HashMap<String, T> getSharedAccessPolicies() {
        return this.sharedAccessPolicies;
    }

    /**
     * Sets the shared access policies.
     * 
     * @param sharedAccessPolicies
     *        A <code>java.util.HashMap</code> object of {@link SharedAccessPolicy} objects
     *        which contain the shared access policies to set.
     */
    public void setSharedAccessPolicies(final HashMap<String, T> sharedAccessPolicies) {
        this.sharedAccessPolicies = sharedAccessPolicies;
    }
}
