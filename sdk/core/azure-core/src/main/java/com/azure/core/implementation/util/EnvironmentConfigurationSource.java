package com.azure.core.implementation.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnvironmentConfigurationSource implements ConfigurationSource {

    /*
     * Configurations that are loaded into the global configuration store when the application starts.
     */
    private static final List<String> DEFAULT_CONFIGURATIONS = List.of(
        Configuration.PROPERTY_HTTP_PROXY,
        Configuration.PROPERTY_HTTPS_PROXY,
        Configuration.PROPERTY_IDENTITY_ENDPOINT,
        Configuration.PROPERTY_IDENTITY_HEADER,
        Configuration.PROPERTY_NO_PROXY,
        Configuration.PROPERTY_MSI_ENDPOINT,
        Configuration.PROPERTY_MSI_SECRET,
        Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID,
        Configuration.PROPERTY_AZURE_USERNAME,
        Configuration.PROPERTY_AZURE_PASSWORD,
        Configuration.PROPERTY_AZURE_CLIENT_ID,
        Configuration.PROPERTY_AZURE_CLIENT_SECRET,
        Configuration.PROPERTY_AZURE_TENANT_ID,
        Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH,
        Configuration.PROPERTY_AZURE_IDENTITY_DISABLE_CP1,
        Configuration.PROPERTY_AZURE_RESOURCE_GROUP,
        Configuration.PROPERTY_AZURE_CLOUD,
        Configuration.PROPERTY_AZURE_AUTHORITY_HOST,
        Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED,
        Configuration.PROPERTY_AZURE_LOG_LEVEL,
        Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL,
        Configuration.PROPERTY_AZURE_TRACING_DISABLED,
        Configuration.PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL,
        Configuration.PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME,
        Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT,
        Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT,
        Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
        Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT,
        Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT
    );

    @Override
    public Iterable<String> getChildKeys(String path) {
        if (path == null) {
            return DEFAULT_CONFIGURATIONS;
        }

        List<String> childKeys = Collections.emptyList();
        for (String prop : DEFAULT_CONFIGURATIONS) {
            if (prop.startsWith(path) && prop.length() > path.length() && prop.charAt(path.length()) == '.') {
                if (childKeys.isEmpty()) {
                    childKeys = new ArrayList<>();
                }

                childKeys.add(prop);
            }
        }

        return childKeys;
    }

    @Override
    public String getValue(String propertyName) {
        String value = System.getProperty(propertyName);

        if (value != null) {
            return value;
        }

        return System.getenv(propertyName);
    }
}
