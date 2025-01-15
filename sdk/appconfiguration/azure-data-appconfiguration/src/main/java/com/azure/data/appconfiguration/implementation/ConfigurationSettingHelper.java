// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.time.OffsetDateTime;

/**
 * Helper class to access private values of {@link ConfigurationSetting} across package boundaries.
 */
public final class ConfigurationSettingHelper {
    private static ConfigurationSettingAccessor accessor;

    private ConfigurationSettingHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ConfigurationSetting} instance.
     */
    public interface ConfigurationSettingAccessor {
        ConfigurationSetting setReadOnly(ConfigurationSetting setting, boolean readOnly);

        ConfigurationSetting setLastModified(ConfigurationSetting setting, OffsetDateTime lastModified);
    }

    /**
     * The method called from {@link ConfigurationSetting} to set its accessor.
     *
     * @param configurationSettingAccessor The accessor.
     */
    public static void setAccessor(final ConfigurationSettingAccessor configurationSettingAccessor) {
        accessor = configurationSettingAccessor;
    }

    public static ConfigurationSetting setReadOnly(ConfigurationSetting setting, boolean readOnly) {
        return accessor.setReadOnly(setting, readOnly);
    }

    public static ConfigurationSetting setLastModified(ConfigurationSetting setting, OffsetDateTime lastModified) {
        return accessor.setLastModified(setting, lastModified);
    }
}
