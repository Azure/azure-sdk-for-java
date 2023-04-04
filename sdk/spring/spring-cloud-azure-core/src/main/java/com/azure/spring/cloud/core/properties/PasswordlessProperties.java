// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties;

import com.azure.spring.cloud.core.implementation.properties.AzurePasswordlessPropertiesMapping;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

import java.util.Properties;

/**
 * Unified properties for Azure passwordless clients.
 */
public interface PasswordlessProperties extends TokenCredentialOptionsProvider, AzureProfileOptionsProvider {

    /**
     * Get the scopes required for the access token.
     *
     * @return scopes required for the access token
     */
    String getScopes();

    /**
     * Set the scopes to get the access token.
     *
     * @param scopes the scopes required for the access token
     */
    void setScopes(String scopes);

    /**
     * Whether to enable connections authenticating with Azure AD, default is false.
     *
     * @return Whether to enable connections authenticating with Azure AD.
     */
    boolean isPasswordlessEnabled();

    /**
     * Set the passwordlessEnabled value.
     *
     * @param passwordlessEnabled the passwordlessEnabled
     */
    void setPasswordlessEnabled(boolean passwordlessEnabled);

    /**
     * Convert {@link PasswordlessProperties} to {@link Properties}.
     * @return converted {@link Properties} instance
     */
    default Properties toPasswordlessProperties() {
        Properties target = new Properties();
        for (AzurePasswordlessPropertiesMapping m : AzurePasswordlessPropertiesMapping.values()) {
            if (m.getter.apply(this) != null) {
                m.setter.accept(target, m.getter.apply(this));
            }
        }
        return target;
    }

}
