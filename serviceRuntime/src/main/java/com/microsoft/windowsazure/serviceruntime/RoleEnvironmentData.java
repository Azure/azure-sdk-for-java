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

import java.util.Map;

/**
 * 
 */
class RoleEnvironmentData {
    private final String deploymentId;
    private final Map<String, String> configurationSettings;
    private final Map<String, LocalResource> localResources;
    private final RoleInstance currentInstance;
    private final Map<String, Role> roles;
    private final boolean isEmulated;

    public RoleEnvironmentData(String deploymentId,
            Map<String, String> configurationSettings,
            Map<String, LocalResource> localResources,
            RoleInstance currentInstance, Map<String, Role> roles,
            boolean isEmulated) {
        this.deploymentId = deploymentId;
        this.configurationSettings = configurationSettings;
        this.localResources = localResources;
        this.currentInstance = currentInstance;
        this.roles = roles;
        this.isEmulated = isEmulated;
    }

    public Map<String, String> getConfigurationSettings() {
        return configurationSettings;
    }

    public Map<String, LocalResource> getLocalResources() {
        return localResources;
    }

    public RoleInstance getCurrentInstance() {
        return currentInstance;
    }

    public Map<String, Role> getRoles() {
        return roles;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public boolean isEmulated() {
        return isEmulated;
    }
}
