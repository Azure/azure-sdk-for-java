// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.core.util.Configuration;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.CLIENT_CERTIFICATE_PATH;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.CLIENT_ID;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.CLIENT_SECRET;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.PASSWORD;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.TENANT_ID;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.USERNAME;

public final class AzureEnvironment {

    private static final String AZURE_CREDENTIAL_PREFIX = "azure.credential.";
    private final Environment environment;
    private final Configuration sdkConfiguration = Configuration.getGlobalConfiguration();

    AzureEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getTenantId() {
        return getPropertyValue(AZURE_CREDENTIAL_PREFIX + TENANT_ID,
                                sdkEnvValue(Configuration.PROPERTY_AZURE_TENANT_ID));
    }

    public String getClientId() {
        return getPropertyValue(AZURE_CREDENTIAL_PREFIX + CLIENT_ID,
                                sdkEnvValue(Configuration.PROPERTY_AZURE_CLIENT_ID));
    }

    public String getClientSecret() {
        return getPropertyValue(AZURE_CREDENTIAL_PREFIX + CLIENT_SECRET,
                                sdkEnvValue(Configuration.PROPERTY_AZURE_CLIENT_ID));
    }

    public String getClientCertificatePath() {
        return getPropertyValue(AZURE_CREDENTIAL_PREFIX + CLIENT_CERTIFICATE_PATH,
                                sdkEnvValue(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH));
    }

    public String getUsername() {
        return getPropertyValue(AZURE_CREDENTIAL_PREFIX + USERNAME,
                                sdkEnvValue(Configuration.PROPERTY_AZURE_USERNAME));
    }

    public String getPassword() {
        return getPropertyValue(AZURE_CREDENTIAL_PREFIX + PASSWORD,
                                sdkEnvValue(Configuration.PROPERTY_AZURE_PASSWORD));
    }

    public String getPropertyValue(String propertyKey) {
        return getPropertyValue(propertyKey, null);
    }

    public String getPropertyValue(String propertyKey, String defaultValue) {
        return Binder.get(this.environment).bind(propertyKey, String.class).orElse(defaultValue);
    }

    private String sdkEnvValue(String sdkPropertyKey) {
        return this.sdkConfiguration.get(sdkPropertyKey);
    }

    public static class AzureSpringPropertyConstants {
        public static String CLIENT_ID = "client-id";
        public static String CLIENT_SECRET = "client-secret";
        public static String CLIENT_CERTIFICATE_PATH = "client-certificate-path";
        public static String TENANT_ID = "tenant-id";
        public static String USERNAME = "username";
        public static String PASSWORD = "password";

    }

}
