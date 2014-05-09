/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.util.Collection;

/**
 * Occurs after a change to the service configuration has been applied to the
 * running instances of the role.
 */
public class RoleEnvironmentChangedEvent {

    private Collection<RoleEnvironmentChange> changes;

    /**
     * Constructor. Can only be called by the deserialization logic
     * 
     * @param changes
     */
    RoleEnvironmentChangedEvent(Collection<RoleEnvironmentChange> changes) {
        this.changes = changes;
    }

    /**
     * Returns a collection of the configuration changes that were applied to
     * the role instance.
     * 
     * @return A <code>java.util.Collection</code> object containing the
     *         {@link RoleEnvironmentChange} objects that represent the
     *         configuration changes that were applied to the role instance.
     * 
     * @see RoleEnvironmentChange
     */
    public Collection<RoleEnvironmentChange> getChanges() {
        return changes;
    }

}
