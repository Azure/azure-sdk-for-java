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
 * Represents a change to the topology of the service.
 * <p>
 * The service's topology refers to the number of instances deployed for each
 * role that the service defines.
 */
public class RoleEnvironmentTopologyChange extends RoleEnvironmentChange {

    private String roleName;

    RoleEnvironmentTopologyChange(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Returns the name of the affected role.
     * 
     * @return A <code>String</code> object that represents the name of the
     *         affected role.
     */
    public String getRoleName() {
        return roleName;
    }

}
