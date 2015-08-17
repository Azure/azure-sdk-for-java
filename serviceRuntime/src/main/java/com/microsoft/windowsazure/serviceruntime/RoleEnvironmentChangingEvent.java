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
 * 
 * Occurs before a change to the service configuration is applied to the running
 * instances of the role.
 * <p>
 * Service configuration changes are applied on-the-fly to running role
 * instances. Configuration changes include changes to the service configuration
 * changes and changes to the number of instances in the service.
 * <p>
 * This event occurs after the new configuration file has been submitted to
 * Windows Azure but before the changes have been applied to each running role
 * instance. This event can be cancelled for a given instance to prevent the
 * configuration change.
 * <p>
 * Note that cancelling this event causes the instance to be automatically
 * recycled. When the instance is recycled, the configuration change is applied
 * when it restarts.
 * 
 * @see RoleEnvironmentChangingListener
 * @see RoleEnvironmentChangedEvent
 */
public class RoleEnvironmentChangingEvent {
    private final Collection<RoleEnvironmentChange> changes;
    private boolean cancelled;

    RoleEnvironmentChangingEvent(Collection<RoleEnvironmentChange> changes) {
        this.changes = changes;

        cancelled = false;
    }

    /**
     * Cancel the configuration change.
     * <p>
     * Cancellation causes the role instance to be immediately recycled. The
     * configuration changes are applied when the instance restarts.
     * 
     */
    public void cancel() {
        cancelled = true;
    }

    boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns a collection of the configuration changes that are about to be
     * applied to the role instance.
     * 
     * @return A <code>java.util.Collection</code> object containing
     *         {@link RoleEnvironmentChange} objects that represent the
     *         configuration changes that are about to be applied to the role
     *         instance.
     * 
     * @see RoleEnvironmentChange
     */
    public Collection<RoleEnvironmentChange> getChanges() {
        return changes;
    }
}
