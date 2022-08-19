// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.enums;


import com.azure.identity.AzureAuthorityHosts;

import java.util.Map;
import java.util.Properties;

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
     * Path of a PEM certificate file to use when performing service principal authentication with Azure.
     */
    CLIENT_CERTIFICATE_PATH("azure.clientCertificatePath",
        "Path of a PEM certificate file to use when performing service principal authentication with Azure.",
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
        false),
    /**
     * Whether to cache an access token.
     */
    CACHE_ENABLED("azure.cacheEnabled",
        "Whether to cache an access token.",
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
        return properties.getProperty(this.propertyKey, defaultValue);
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
}
