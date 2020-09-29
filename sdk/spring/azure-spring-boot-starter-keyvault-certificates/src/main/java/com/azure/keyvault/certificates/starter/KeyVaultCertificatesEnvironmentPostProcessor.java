// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.keyvault.certificates.starter;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import java.security.Security;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(LOWEST_PRECEDENCE)
public class KeyVaultCertificatesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
            SpringApplication application) {

        Properties properties = System.getProperties();
        
        String uri = environment.getProperty("azure.keyvault.uri");
        if (uri != null) {
            properties.put("azure.keyvault.uri", uri);
        }

        String tenantId = environment.getProperty("azure.keyvault.tenantId");
        if (tenantId != null) {
            properties.put("azure.keyvault.tenantId", tenantId);
        }

        String clientId = environment.getProperty("azure.keyvault.clientId");
        if (clientId != null) {
            properties.put("azure.keyvault.clientId", clientId);
        }

        String clientSecret = environment.getProperty("azure.keyvault.clientSecret");
        if (clientSecret != null) {
            properties.put("azure.keyvault.clientSecret", clientSecret);
        }

        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.insertProviderAt(provider, 1);
    }
}
