// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.keyvault.certificates.starter;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultTrustManagerFactoryProvider;

import java.security.Security;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * Leverage {@link EnvironmentPostProcessor} to add Key Store property source.
 */
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
            
            String aadAuthenticationUrl = environment.getProperty("azure.keyvault.aadAuthenticationUrl");
            if (aadAuthenticationUrl != null) {
                systemProperties.put("azure.keyvault.aadAuthenticationUrl", aadAuthenticationUrl);
            }

            String clientId = environment.getProperty("azure.keyvault.clientId");
            if (clientId != null) {
                systemProperties.put("azure.keyvault.clientId", clientId);
            }

            String clientSecret = environment.getProperty("azure.keyvault.clientSecret");
            if (clientSecret != null) {
                systemProperties.put("azure.keyvault.clientSecret", clientSecret);
            }

            String managedIdentity = environment.getProperty("azure.keyvault.managedIdentity");
            if (managedIdentity != null) {
                systemProperties.put("azure.keyvault.managedIdentity", managedIdentity);
            }

            String keyStoreType = environment.getProperty("server.ssl.key-store-type");

            if (keyStoreType != null && keyStoreType.equals("AzureKeyVault")) {
                MutablePropertySources sources = environment.getPropertySources();
                Properties properties = new Properties();
                properties.put("server.ssl.key-store", "classpath:keyvault.dummy");

                try {
                    Class.forName("org.apache.tomcat.InstanceManager");
                    properties.put("server.ssl.key-store-type", "DKS");
                } catch (ClassNotFoundException ex) {
                }

                PropertiesPropertySource propertySource
                        = new PropertiesPropertySource("KeyStorePropertySource", properties);
                sources.addFirst(propertySource);
            }

            String trustStoreType = environment.getProperty("server.ssl.trust-store-type");

            if (trustStoreType != null && trustStoreType.equals("AzureKeyVault")) {
                MutablePropertySources sources = environment.getPropertySources();
                Properties properties = new Properties();
                properties.put("server.ssl.trust-store", "classpath:keyvault.dummy");

                try {
                    Class.forName("org.apache.tomcat.InstanceManager");
                    properties.put("server.ssl.trust-store-type", "DKS");
                } catch (ClassNotFoundException ex) {
                }

                PropertiesPropertySource propertySource
                        = new PropertiesPropertySource("TrustStorePropertySource", properties);
                sources.addFirst(propertySource);
            }

            KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
            Security.insertProviderAt(provider, 1);

            String enabled = environment.getProperty("azure.keyvault.jca.overrideTrustManagerFactory");
            if (Boolean.parseBoolean(enabled)) {
                KeyVaultTrustManagerFactoryProvider factoryProvider
                        = new KeyVaultTrustManagerFactoryProvider();
                Security.insertProviderAt(factoryProvider, 1);
            }

            enabled = environment.getProperty("azure.keyvault.jca.disableHostnameVerification");
            if (Boolean.parseBoolean(enabled)) {
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            }
        }
    }
}
