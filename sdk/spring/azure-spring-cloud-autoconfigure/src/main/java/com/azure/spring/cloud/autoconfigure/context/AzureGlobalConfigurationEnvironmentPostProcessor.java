// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.util.Configuration;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_SECRET;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_PASSWORD;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_TENANT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_USERNAME;

/**
 * An EnvironmentPostProcessor to set spring.cloud.azure.* properties to Azure SDK global configuration.
 */
public class AzureGlobalConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String CREDENTIAL_PREFIX = AzureConfigurationProperties.PREFIX + ".credential.";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        final Configuration globalConfiguration = Configuration.getGlobalConfiguration();

        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(environment.getProperty(CREDENTIAL_PREFIX + "client-id"))
                      .to(p -> globalConfiguration.put(PROPERTY_AZURE_CLIENT_ID, p));
        propertyMapper.from(environment.getProperty(CREDENTIAL_PREFIX + "client-secret"))
                      .to(p -> globalConfiguration.put(PROPERTY_AZURE_CLIENT_SECRET, p));
        propertyMapper.from(environment.getProperty(CREDENTIAL_PREFIX + "tenant-id"))
                      .to(p -> globalConfiguration.put(PROPERTY_AZURE_TENANT_ID, p));
        propertyMapper.from(environment.getProperty(CREDENTIAL_PREFIX + "client-certificate-path"))
                      .to(p -> globalConfiguration.put(PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, p));
        propertyMapper.from(environment.getProperty(CREDENTIAL_PREFIX + "username"))
                      .to(p -> globalConfiguration.put(PROPERTY_AZURE_USERNAME, p));
        propertyMapper.from(environment.getProperty(CREDENTIAL_PREFIX + "password"))
                      .to(p -> globalConfiguration.put(PROPERTY_AZURE_PASSWORD, p));

    }
}
