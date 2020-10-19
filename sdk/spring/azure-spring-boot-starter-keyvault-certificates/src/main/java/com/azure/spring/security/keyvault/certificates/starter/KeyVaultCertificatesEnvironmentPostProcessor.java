// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.security.keyvault.certificates.starter;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultTrustManagerFactoryProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.net.ssl.HttpsURLConnection;
import java.security.Security;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Order
public class KeyVaultCertificatesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultCertificatesEnvironmentPostProcessor.class.getName());

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        if (environment.getProperty("azure.keyvault.uri") == null) {
            return;
        }
        Properties systemProperties = System.getProperties();
        Stream.of("uri", "tenantId", "clientId", "clientSecret")
              .forEach(key -> {
                  String property = "azure.keyvault." + key;
                  Optional.of(property)
                          .map(environment::getProperty)
                          .ifPresent(value -> systemProperties.put(property, value));
              });
        String keyStoreType = environment.getProperty("server.ssl.key-store-type");
        if (keyStoreType != null && keyStoreType.equals("AzureKeyVault")) {
            MutablePropertySources sources = environment.getPropertySources();
            Properties properties = new Properties();
            properties.put("server.ssl.key-store", "classpath:keyvault.dummy");
            try {
                Class.forName("org.apache.tomcat.InstanceManager");
                properties.put("server.ssl.key-store-type", "DKS");
            } catch (ClassNotFoundException ignored) {
            }
            PropertySource propertySource = new PropertiesPropertySource("KeyStorePropertySource", properties);
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
            } catch (ClassNotFoundException ignored) {
            }
            PropertySource propertySource = new PropertiesPropertySource("TrustStorePropertySource", properties);
            sources.addFirst(propertySource);
        }

        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.insertProviderAt(provider, 1);
        String enabled = environment.getProperty("azure.keyvault.jca.overrideTrustManagerFactory");
        if (Boolean.parseBoolean(enabled)) {
            KeyVaultTrustManagerFactoryProvider factoryProvider =
                new KeyVaultTrustManagerFactoryProvider();
            Security.insertProviderAt(factoryProvider, 1);
        }
        enabled = environment.getProperty("azure.keyvault.jca.disableHostnameVerification");
        if (Boolean.parseBoolean(enabled)) {
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        }
    }
}
