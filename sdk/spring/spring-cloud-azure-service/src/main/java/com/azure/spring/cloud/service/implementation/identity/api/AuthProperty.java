package com.azure.spring.cloud.service.implementation.identity.api;

import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;

import java.util.Map;
import java.util.Properties;

public enum AuthProperty {

    CLIENT_ID("azure.clientId"),
    CLIENT_SECRET("azure.clientSecret"),
    CLIENT_CERTIFICATE_PATH("azure.clientCertificatePath"),
    CLIENT_CERTIFICATE_PASSWORD("azure.clientCertificatePassword"),
    USERNAME("azure.username"),
    PASSWORD("azure.password"),
    MANAGED_IDENTITY_ENABLED("azure.managedIdentityEnabled"),
    AUTHORITY_HOST("azure.authorityHost", AzureAuthorityHosts.AZURE_PUBLIC_CLOUD),

    TENANT_ID("azure.tenantId"),
    CLAIMS("azure.claims"),
    SCOPES("azure.scopes"),

    // TODO define this here?,
    GET_TOKEN_TIMEOUT("azure.accessTokenTimeoutInSeconds"),
    TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME("azure.tokenCredentialProviderClassName"),
    TOKEN_CREDENTIAL_BEAN_NAME("azure.tokenCredentialBeanName"),
    CACHE_ENABLED("azure.cacheEnabled")
    ;

    String propertyKey;
    String defaultValue;

    AuthProperty(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    AuthProperty(String propertyKey, String defaultValue) {
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
    }

    public String get(Configuration configuration) {
        return configuration.get(this.propertyKey, defaultValue);
    }

    public Boolean getBoolean(Configuration configuration) {
        return Boolean.parseBoolean(get(configuration));
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public Integer getInteger(Configuration configuration) {
        return Integer.parseInt(get(configuration));
    }

    public void setProperty(Configuration configuration, String value) {
        if (value == null) {
            configuration.remove(this.propertyKey);
        } else {
            configuration.put(this.propertyKey, value);
        }
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
