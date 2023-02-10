// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * AzureServiceBusCredentialSupplier that provide a String as the password to connect Azure ServiceBus.
 *
 * @since 4.7.0
 */
public class AzureServiceBusCredentialSupplier implements Supplier<String> {

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    /**
     * Create {@link AzureServiceBusCredentialSupplier} instance.
     * @param properties properties to initialize AzureServiceBusCredentialSupplier.
     */
    public AzureServiceBusCredentialSupplier(Properties properties) {
        azureAuthenticationTemplate = new AzureAuthenticationTemplate();
        azureAuthenticationTemplate.init(properties);
    }

    @Override
    public String get() {
        return azureAuthenticationTemplate.getTokenAsPassword();
    }

    AzureServiceBusCredentialSupplier(AzureAuthenticationTemplate azureAuthenticationTemplate) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
    }
}
