// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Configuration Audience is used to specify the target audience for the Azure App Configuration service.
 * Microsoft Entra audience is configurable via the {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#audience(ConfigurationAudience)} method.
 */
public final class ConfigurationAudience extends ExpandableStringEnum<ConfigurationAudience> {
    /**
     * The Azure App Configuration service audience for China Cloud.
     */
    public static final ConfigurationAudience AZURE_CHINA = fromString("https://appconfig.azure.cn");

    /**
     * The Azure App Configuration service audience for US Government Cloud.
     */
    public static final ConfigurationAudience AZURE_GOVERNMENT = fromString("https://appconfig.azure.us");

    /**
     * The Azure App Configuration service audience for Public Cloud.
     */
    public static final ConfigurationAudience AZURE_PUBLIC_CLOUD = fromString("https://appconfig.azure.com");

    /**
     * Creates a new instance of ConfigurationAudience value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ConfigurationAudience() {
    }

    /**
     * Creates or finds a ConfigurationAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ConfigurationAudience.
     */
    public static ConfigurationAudience fromString(String name) {
        return fromString(name, ConfigurationAudience.class);
    }

    /**
     * Gets known ConfigurationAudience values.
     *
     * @return known ConfigurationAudience values.
     */
    public static Collection<ConfigurationAudience> values() {
        return values(ConfigurationAudience.class);
    }
}
