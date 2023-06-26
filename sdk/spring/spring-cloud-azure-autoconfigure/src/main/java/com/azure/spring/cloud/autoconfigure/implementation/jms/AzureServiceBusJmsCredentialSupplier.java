// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * AzureServiceBusJmsCredentialSupplier that provides a String as the password to connect Azure ServiceBus.
 *
 * @since 4.7.0
 */
public class AzureServiceBusJmsCredentialSupplier implements Supplier<String> {

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    /**
     * Create {@link AzureServiceBusJmsCredentialSupplier} instance.
     * @param properties properties to initialize AzureServiceBusJmsCredentialSupplier.
     */
    public AzureServiceBusJmsCredentialSupplier(Properties properties) {
        azureAuthenticationTemplate = new AzureAuthenticationTemplate();
        azureAuthenticationTemplate.init(properties);
    }

    @Override
    public String get() {
        return azureAuthenticationTemplate.getTokenAsPassword();
    }

    AzureServiceBusJmsCredentialSupplier(AzureAuthenticationTemplate azureAuthenticationTemplate) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
    }
}
