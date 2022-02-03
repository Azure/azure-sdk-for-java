// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationDoc;
import com.azure.core.util.ConfigurationProperty;

/**
 * Represents a credential bag containing the key and the name of the key.
 *
 * @see AzureNamedKeyCredential
 */
@Immutable
public final class AzureNamedKey {
    private final String name;
    private final String key;

    AzureNamedKey(String name, String key) {
        this.name = name;
        this.key = key;
    }

    /**
     * Retrieves the key.
     *
     * @return The key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Retrieves the name associated with the key.
     *
     * @return The name of the key.
     */
    public String getName() {
        return name;
    }

    @ConfigurationDoc(description = "named key name.")
    private final static ConfigurationProperty<String> NAME_PROPERTY = ConfigurationProperty.stringPropertyBuilder("credential.named-key.name")
        .canLogValue(true)
        .build();

    @ConfigurationDoc(description = "named key key.")
    private final static ConfigurationProperty<String> KEY_PROPERTY = ConfigurationProperty.stringPropertyBuilder("credential.named-key.key")
        .build();


    public static AzureNamedKey fromConfiguration(Configuration configuration, AzureNamedKey defaultValue) {
        String name = configuration.get(NAME_PROPERTY);
        String key = configuration.get(KEY_PROPERTY);
        if (name == null && key == null) {
            return defaultValue;
        }

        return new AzureNamedKey(name, key);
    }
}
