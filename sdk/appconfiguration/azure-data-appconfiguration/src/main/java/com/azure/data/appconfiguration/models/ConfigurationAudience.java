// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

public final class ConfigurationAudience extends ExpandableStringEnum<ConfigurationAudience> {
    public static final ConfigurationAudience AzureChina = fromString("https://appconfig.azure.cn");
    public static final ConfigurationAudience AzureGovernment = fromString("https://appconfig.azure.us");
    public static final ConfigurationAudience AzurePublicCloud = fromString("https://appconfig.azure.com");

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
