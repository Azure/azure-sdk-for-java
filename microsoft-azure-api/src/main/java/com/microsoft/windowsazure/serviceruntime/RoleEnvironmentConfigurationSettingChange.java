package com.microsoft.windowsazure.serviceruntime;

/**
 * Represents a change to a configuration setting.
 * 
 * @see RoleEnvironmentTopologyChange
 */
public class RoleEnvironmentConfigurationSettingChange extends RoleEnvironmentChange {

    private String settingName;

    RoleEnvironmentConfigurationSettingChange(String settingName) {
        this.settingName = settingName;
    }

    /**
     * Returns the name of the configuration setting that has been changed.
     * 
     * @return A <code>String</code> object that represents the name of the
     *         configuration setting that has been changed.
     */
    public String getConfigurationSettingName() {
        return settingName;
    }

}