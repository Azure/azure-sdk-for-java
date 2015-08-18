/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.credentials.CloudCredentials;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

/**
 * Helper class to create or modify Azure Key Vault client configurations.
 *
 */
public final class KeyVaultConfiguration {

    private KeyVaultConfiguration() {
        // not instantiable
    }

    /**
     * Creates a new configuration.
     *
     * @param profile
     *            The parent profile. If <code>null</code>, the configuration
     *            will not be bound to any profile.
     * @param credentials
     *            An instance of {@link CloudCredentials}. May be
     *            <code>null</code>, but must be provided elsewhere.
     * @return
     */
    public static Configuration configure(String profile, CloudCredentials credentials) {
        Configuration configuration = Configuration.getInstance();
        return configure(configuration, profile, credentials);
    }

    /**
     * Modifies an existing configuration.
     *
     * @param configuration
     *            The configuration to be modified. If <code>null</code>, this
     *            method may generate a {@link NullPointerException}.
     * @param profile
     *            The parent profile. If <code>null</code>, the configuration
     *            will not be bound to any profile.
     * @param credentials
     *            An instance of {@link CloudCredentials}. May be
     *            <code>null</code>, but must be provided elsewhere.
     * @return The value of <code>configuration</code> parameter.
     */
    public static Configuration configure(Configuration configuration, String profile, CloudCredentials credentials) {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        if (credentials != null) {
            configuration.setProperty(profile + ManagementConfiguration.SUBSCRIPTION_CLOUD_CREDENTIALS, credentials);
        }

        // configuration.setProperty(profile +
        // ApacheConfigurationProperties.PROPERTY_REDIRECT_STRATEGY, new
        // LaxRedirectStrategy());
        return configuration;
    }

}
