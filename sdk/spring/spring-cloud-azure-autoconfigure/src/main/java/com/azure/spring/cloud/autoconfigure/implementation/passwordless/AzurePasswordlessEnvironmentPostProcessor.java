// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.passwordless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Add properties to 'spring.cloud.function.ineligible-definitions' to filter ineligible functions that used by passwordless autoconfigurations.
 *
 * @since 4.7.0
 */
public class AzurePasswordlessEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * The order value of the {@link AzurePasswordlessEnvironmentPostProcessor}.
     */
    public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 2;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();
        List<String> passwordlessCredentialSupplier = new ArrayList<>();
        passwordlessCredentialSupplier.add("azureServiceBusJmsCredentialSupplier");
        properties.setProperty("spring.cloud.function.ineligible-definitions", String.join(",", passwordlessCredentialSupplier));
        environment.getPropertySources().addLast(new PropertiesPropertySource("passwordless", properties));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
