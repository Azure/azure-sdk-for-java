// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.enums;

import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.azure.identity.providers.jdbc.implementation.enums.AuthProperty.AuthPropertyConfigurationProperty.getConfigurationPropertyValue;

/**
 * Contains authentication property used to resolve token credential.
 */
public enum AuthProperty {
    /**
     * Client ID to use when performing service principal authentication with Azure.
     */
    CLIENT_ID("azure.clientId",
        "Client ID to use when performing service principal authentication with Azure.",
        false),
    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    CLIENT_SECRET("azure.clientSecret",
        "Client secret to use when performing service principal authentication with Azure.",
        false),
    /**
     * Path of a PEM/PFX certificate file to use when performing service principal authentication with Azure.
     */
    CLIENT_CERTIFICATE_PATH("azure.clientCertificatePath",
        "Path of a PEM/PFX certificate file to use when performing service principal authentication with Azure.",
        false),
    /**
     * Password of the certificate file.
     */
    CLIENT_CERTIFICATE_PASSWORD("azure.clientCertificatePassword",
        "Password of the certificate file.",
        false),
    /**
     * Username to use when performing username/password authentication with Azure.
     */
    USERNAME("azure.username",
        "Username to use when performing username/password authentication with Azure.",
        false),
    /**
     * Password to use when performing username/password authentication with Azure.
     */
    PASSWORD("azure.password",
        "Password to use when performing username/password authentication with Azure.",
        false),
    /**
     *  Whether to enable managed identity to authenticate with Azure.
     */
    MANAGED_IDENTITY_ENABLED("azure.managedIdentityEnabled",
        "Whether to enable managed identity to authenticate with Azure.",
        false),
    /**
     * The well known authority hosts for the Azure Public Cloud and sovereign clouds.
     */
    AUTHORITY_HOST("azure.authorityHost",
        AzureAuthorityHosts.AZURE_PUBLIC_CLOUD,
        "The well known authority hosts for the Azure Public Cloud and sovereign clouds.",
        true),
    /**
     * Tenant ID for Azure resources.
     */
    TENANT_ID("azure.tenantId",
        "Tenant ID for Azure resources.",
        true),
    /**
     * Claims for Azure resources.
     */
    CLAIMS("azure.claims",
        "Claims for Azure resources.",
        false),
    /**
     * Scopes for Azure resources.
     */
    SCOPES("azure.scopes",
        "Scopes for Azure resources.",
        false),
    /**
     * Max time to get an access token.
     */
    GET_TOKEN_TIMEOUT("azure.accessTokenTimeoutInSeconds",
        "Max time to get an access token.",
        false),
    /**
     * The canonical class name of a class that implements 'TokenCredentialProvider'.
     */
    TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME("azure.tokenCredentialProviderClassName",
        "The canonical class name of a class that implements 'TokenCredentialProvider'.",
        false),
    /**
     * The given bean name of a TokenCredential bean in the Spring context.
     */
    TOKEN_CREDENTIAL_BEAN_NAME("azure.tokenCredentialBeanName",
        "springCloudAzureDefaultCredential",
        "The given bean name of a TokenCredential bean in the Spring context.",
        false);

    String propertyKey;
    String defaultValue;
    String description;
    boolean required;

    AuthProperty(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    AuthProperty(String propertyKey, String description, boolean required) {
        this.propertyKey = propertyKey;
        this.description = description;
        this.required = required;
    }

    AuthProperty(String propertyKey, String defaultValue, String description, boolean required) {
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
        this.description = description;
        this.required = required;
    }

    /**
     * Get propertyValue from properties.
     * @param properties A set of properties.
     * @return The propertyValue.
     */
    public String get(Properties properties) {
        return properties.getProperty(this.propertyKey, getConfigurationPropertyValue(this));
    }

    /**
     * Get the Boolean value form properties.
     *
     * @param properties A set of properties.
     * @return Boolean type value.
     */
    public Boolean getBoolean(Properties properties) {
        return Boolean.parseBoolean(get(properties));
    }

    /**
     * Get the propertyKey.
     *
     * @return the propertyKey
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * Get the Integer value form properties.
     *
     * @param properties A set of properties.
     * @return Integer type value.
     */
    public Integer getInteger(Properties properties) {
        return Integer.parseInt(get(properties));
    }

    /**
     * Set properties with given value.
     * @param properties A set of properties.
     * @param value A String value represents the property value.
     */
    public void setProperty(Properties properties, String value) {
        if (value == null) {
            properties.remove(this.propertyKey);
        } else {
            properties.put(this.propertyKey, value);
        }
    }

    /**
     * Set map with given value.
     * @param map A map contains key values.
     * @param value A String value represents the property value.
     */
    public void setProperty(Map<String, String> map, String value) {
        if (value == null) {
            map.remove(this.propertyKey);
        } else {
            map.put(this.propertyKey, value);
        }
    }

    static class AuthPropertyConfigurationProperty {
        private static Configuration configuration = Configuration.getGlobalConfiguration();
        static Map<AuthProperty, String> authPropertyKeyAndConfigurationPropertyKey = new HashMap<AuthProperty, String>() {
            {
                put(AuthProperty.CLIENT_ID, Configuration.PROPERTY_AZURE_CLIENT_ID);
                put(AuthProperty.CLIENT_SECRET, Configuration.PROPERTY_AZURE_CLIENT_SECRET);
                put(AuthProperty.CLIENT_CERTIFICATE_PATH, Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
                put(AuthProperty.USERNAME, Configuration.PROPERTY_AZURE_USERNAME);
                put(AuthProperty.PASSWORD, Configuration.PROPERTY_AZURE_PASSWORD);
                put(AuthProperty.AUTHORITY_HOST, Configuration.PROPERTY_AZURE_AUTHORITY_HOST);
                put(AuthProperty.TENANT_ID, Configuration.PROPERTY_AZURE_TENANT_ID);
            }
        };

        static String getConfigurationPropertyValue(AuthProperty authProperty) {
            String key = authPropertyKeyAndConfigurationPropertyKey.get(authProperty);
            if (key == null) {
                return authProperty.defaultValue;
            }
            String value = configuration.get(key);
            return value == null ? authProperty.defaultValue : value;
        }
    }
}
