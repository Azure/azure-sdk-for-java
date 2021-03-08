// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.keyvault.certificates.starter;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultTrustManagerFactoryProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.security.Security;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Leverage {@link EnvironmentPostProcessor} to add Key Store property source.
 */
@Order()
public class KeyVaultCertificatesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        if (environment.getProperty("azure.keyvault.uri") == null) {
            return;
        }

        Stream.of("azure.keyvault.uri",
            "azure.keyvault.tenant-id",
            "azure.keyvault.aad-authentication-url",
            "azure.keyvault.client-id",
            "azure.keyvault.client-secret",
            "azure.keyvault.managed-identity")
              .forEach(key -> putEnvironmentPropertyToSystemProperty(environment, key));

        if ("AzureKeyVault".equals(environment.getProperty("server.ssl.key-store-type"))) {
            Properties properties = new Properties();
            properties.put("server.ssl.key-store", "classpath:keyvault.dummy");
            if (hasEmbedTomcat()) {
                properties.put("server.ssl.key-store-type", "DKS");
            }
            environment.getPropertySources().addFirst(
                new PropertiesPropertySource("KeyStorePropertySource", properties));
        }

        if ("AzureKeyVault".equals(environment.getProperty("server.ssl.trust-store-type"))) {
            Properties properties = new Properties();
            properties.put("server.ssl.trust-store", "classpath:keyvault.dummy");
            if (hasEmbedTomcat()) {
                properties.put("server.ssl.trust-store-type", "DKS");
            }
            environment.getPropertySources().addFirst(
                new PropertiesPropertySource("TrustStorePropertySource", properties));
        }

        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        if (environmentPropertyIsTrue(environment, "azure.keyvault.jca.overrideTrustManagerFactory")) {
            Security.insertProviderAt(new KeyVaultTrustManagerFactoryProvider(), 1);
        }

        if (environmentPropertyIsTrue(environment, "azure.keyvault.jca.disableHostnameVerification")) {
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        }
    }

    private void putEnvironmentPropertyToSystemProperty(ConfigurableEnvironment environment, String key) {
        Optional.of(key)
                .map(environment::getProperty)
                .filter(StringUtils::hasText)
                .ifPresent(value -> System.getProperties().put(key, value));
    }

    private boolean hasEmbedTomcat() {
        try {
            Class.forName("org.apache.tomcat.InstanceManager");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private boolean environmentPropertyIsTrue(ConfigurableEnvironment environment, String key) {
        return Optional.of(key)
                       .map(environment::getProperty)
                       .map(Boolean::parseBoolean)
                       .orElse(false);
    }
}
