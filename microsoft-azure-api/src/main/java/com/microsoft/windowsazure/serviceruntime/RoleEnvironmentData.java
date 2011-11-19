/**
 * 
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

    public RoleEnvironmentData(String deploymentId, Map<String, String> configurationSettings,
            Map<String, LocalResource> localResources, RoleInstance currentInstance, Map<String, Role> roles,
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
