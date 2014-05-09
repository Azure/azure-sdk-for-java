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
 * Represents the listener for the stopping event.
 */
public interface RoleEnvironmentStoppingListener {
    /**
     * Occurs when the role instance is about to be stopped.
     * <p>
     * This event is raised after the instance has been taken out of the load
     * balancer's rotation before the <code>OnStop</code> method is called. You
     * can use this event to run code that is required for the role instance to
     * shut down in an orderly fashion.
     */

    void roleEnvironmentStopping();

}
