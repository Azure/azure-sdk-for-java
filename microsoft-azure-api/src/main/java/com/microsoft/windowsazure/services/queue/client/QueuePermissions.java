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

package com.microsoft.windowsazure.services.queue.client;

import java.util.HashMap;

/**
 * Represents the permissions for a container.
 */

public final class QueuePermissions {

    /**
     * Gets the set of shared access policies for the queue.
     */
    private HashMap<String, SharedAccessQueuePolicy> sharedAccessPolicies;

    /**
     * Creates an instance of the <code>QueuePermissions</code> class.
     */
    public QueuePermissions() {
        this.sharedAccessPolicies = new HashMap<String, SharedAccessQueuePolicy>();
    }

    /**
     * Returns the set of shared access policies for the queue.
     * 
     * @return A <code>HashMap</code> object of {@link SharedAccessQueuePolicy} objects that represent the set of shared
     *         access policies for the queue.
     */
    public HashMap<String, SharedAccessQueuePolicy> getSharedAccessPolicies() {
        return this.sharedAccessPolicies;
    }

    /**
     * Sets the set of shared access policies for the queue.
     * 
     * @param sharedAccessPolicies
     *            The set of shared access policies to set for the queue, represented by a <code>HashMap</code> object
     *            of {@link SharedAccessQueuePolicy} objects.
     */
    public void setSharedAccessPolicies(final HashMap<String, SharedAccessQueuePolicy> sharedAccessPolicies) {
        this.sharedAccessPolicies = sharedAccessPolicies;
    }
}