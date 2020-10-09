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
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

@Order(LOWEST_PRECEDENCE)
public class KeyVaultCertificatesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
            SpringApplication application) {

        Properties systemProperties = System.getProperties();

        String uri = environment.getProperty("azure.keyvault.uri");
        if (uri != null) {
            systemProperties.put("azure.keyvault.uri", uri);

            String tenantId = environment.getProperty("azure.keyvault.tenantId");
            if (tenantId != null) {
                systemProperties.put("azure.keyvault.tenantId", tenantId);
            }

            String clientId = environment.getProperty("azure.keyvault.clientId");
            if (clientId != null) {
                systemProperties.put("azure.keyvault.clientId", clientId);
            }

            String clientSecret = environment.getProperty("azure.keyvault.clientSecret");
            if (clientSecret != null) {
                systemProperties.put("azure.keyvault.clientSecret", clientSecret);
            }

            String keyStoreType = environment.getProperty("server.ssl.key-store-type");

            if (keyStoreType != null && (keyStoreType.equals("DKS") || keyStoreType.equals("AzureKeyVault"))) {
                MutablePropertySources sources = environment.getPropertySources();
                Properties properties = new Properties();
                properties.put("server.ssl.key-store", "classpath:keyvault.dummy");
                PropertySource propertySource = new PropertiesPropertySource("KeyStorePropertySource", properties);
                sources.addFirst(propertySource);
            }
            
            String trustStoreType = environment.getProperty("server.ssl.trust-store-type");

            if (trustStoreType != null && (trustStoreType.equals("DKS") || trustStoreType.equals("AzureKeyVault"))) {
                MutablePropertySources sources = environment.getPropertySources();
                Properties properties = new Properties();
                properties.put("server.ssl.trust-store", "classpath:keyvault.dummy");
                PropertySource propertySource = new PropertiesPropertySource("TrustStorePropertySource", properties);
                sources.addFirst(propertySource);
            }

            KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
            Security.insertProviderAt(provider, 1);
        }
    }
}
