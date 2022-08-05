// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.provider.SpringTokenCredentialProvider;

import java.util.Map;
import java.util.Properties;

/**
 * Contains authentication property used to resolve token credential.
 */
public enum AuthProperty {

    CLIENT_ID("azure.clientId",
        "Client ID to use when performing service principal authentication with Azure.",
        false),
    CLIENT_SECRET("azure.clientSecret",
        "Client secret to use when performing service principal authentication with Azure.",
        false),
    CLIENT_CERTIFICATE_PATH("azure.clientCertificatePath",
        "Path of a PEM certificate file to use when performing service principal authentication with Azure.",
        false),
    CLIENT_CERTIFICATE_PASSWORD("azure.clientCertificatePassword",
        "Password of the certificate file.",
        false),
    USERNAME("azure.username",
        "Username to use when performing username/password authentication with Azure.",
        false),
    PASSWORD("azure.password",
        "Password to use when performing username/password authentication with Azure.",
        false),
    MANAGED_IDENTITY_ENABLED("azure.managedIdentityEnabled",
        "Whether to enable managed identity to authenticate with Azure.",
        false),
    AUTHORITY_HOST("azure.authorityHost",
        AzureAuthorityHosts.AZURE_PUBLIC_CLOUD,
        "The well known authority hosts for the Azure Public Cloud and sovereign clouds.",
        true),

    TENANT_ID("azure.tenantId",
        "Tenant ID for Azure resources.",
        true),
    CLAIMS("azure.claims",
        "Claims for Azure resources.",
        false),
    SCOPES("azure.scopes",
        "Scopes for Azure resources.",
        false),

    GET_TOKEN_TIMEOUT("azure.accessTokenTimeoutInSeconds",
        "Max time to get an access token.",
        false),
    TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME("azure.tokenCredentialProviderClassName",
        "The canonical class name of a class that implements 'TokenCredentialProvider'.",
        false),

    TOKEN_CREDENTIAL_BEAN_NAME("azure.tokenCredentialBeanName",
        SpringTokenCredentialProvider.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME,
        "The given bean name of a TokenCredential bean in the Spring context.",
        false),
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

    public String get(Properties properties) {
        return properties.getProperty(this.propertyKey, defaultValue);
    }

    public Boolean getBoolean(Properties properties) {
        return Boolean.parseBoolean(get(properties));
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public Integer getInteger(Properties properties) {
        return Integer.parseInt(get(properties));
    }

    public void setProperty(Properties properties, String value) {
        if (value == null) {
            properties.remove(this.propertyKey);
        } else {
            properties.put(this.propertyKey, value);
        }
    }

    public void setProperty(Map<String, String> map, String value) {
        if (value == null) {
            map.remove(this.propertyKey);
        } else {
            map.put(this.propertyKey, value);
        }
    }
}
