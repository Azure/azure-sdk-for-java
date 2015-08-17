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

/**
 * Represents the listener for the environment changed event.
 * <p>
 * The environment changed event is encapsulated in a
 * {@link RoleEnvironmentChangedEvent} object.
 */
public interface RoleEnvironmentChangedListener {

    /**
     * Occurs after a change to the service configuration has been applied to
     * the running instances of the role.
     * 
     * @param event
     *            A {@link RoleEnvironmentChangedEvent} object that represents
     *            the environment changed event.
     * 
     * @see RoleEnvironmentChangingListener#roleEnvironmentChanging
     */
    void roleEnvironmentChanged(RoleEnvironmentChangedEvent event);
}
