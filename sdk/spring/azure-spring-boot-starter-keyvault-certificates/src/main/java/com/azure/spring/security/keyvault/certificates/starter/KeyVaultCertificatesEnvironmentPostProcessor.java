// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.keyvault.certificates.starter;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;
import com.azure.security.keyvault.jca.KeyVaultTrustManagerFactoryProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.security.Security;
import java.util.Optional;
import java.util.Properties;

/**
 * Leverage {@link EnvironmentPostProcessor} to add Key Store property source.
 */
@Order
public class KeyVaultCertificatesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.uri");
        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.tenant-id");
        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.client-id");
        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.client-secret");
        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.managed-identity");
        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.jca.certificates-refresh-interval");
        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.jca.certificates-refresh-interval-in-ms");
        putEnvironmentPropertyToSystemProperty(environment, "azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate");
        putEnvironmentPropertyToSystemProperty(environment, "azure.cert-path.well-known");
        putEnvironmentPropertyToSystemProperty(environment, "azure.cert-path.custom");

        MutablePropertySources propertySources = environment.getPropertySources();
        if (KeyVaultKeyStore.KEY_STORE_TYPE.equals(environment.getProperty("server.ssl.key-store-type"))) {
            Properties properties = new Properties();
            properties.put("server.ssl.key-store", "classpath:keyvault.dummy");
            if (hasEmbedTomcat()) {
                properties.put("server.ssl.key-store-type", "DKS");
            }
            propertySources.addFirst(new PropertiesPropertySource("KeyStorePropertySource", properties));
        }
        if (KeyVaultKeyStore.KEY_STORE_TYPE.equals(environment.getProperty("server.ssl.trust-store-type"))) {
            Properties properties = new Properties();
            properties.put("server.ssl.trust-store", "classpath:keyvault.dummy");
            if (hasEmbedTomcat()) {
                properties.put("server.ssl.trust-store-type", "DKS");
            }
            propertySources.addFirst(new PropertiesPropertySource("TrustStorePropertySource", properties));
        }

        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        if (overrideTrustManagerFactory(environment)) {
            Security.insertProviderAt(new KeyVaultTrustManagerFactoryProvider(), 1);
        }

        if (disableHostnameVerification(environment)) {
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        }
    }

    /**
     * The method is use to make the properties in "application.properties" readable in azure-security-keyvault-jca.
     *
     * "application.properties" is analyzed by Spring, and azure-security-keyvault-jca does not depends on Spring.
     * Put the properties into System.getProperties() can make them readable in azure-security-keyvault-jca.
     */
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

    static boolean overrideTrustManagerFactory(ConfigurableEnvironment environment) {
        return environmentPropertyIsTrue(environment, "azure.keyvault.jca.overrideTrustManagerFactory")
            || environmentPropertyIsTrue(environment, "azure.keyvault.jca.override-trust-manager-factory");
    }

    private static boolean disableHostnameVerification(ConfigurableEnvironment environment) {
        return environmentPropertyIsTrue(environment, "azure.keyvault.jca.disableHostnameVerification")
            || environmentPropertyIsTrue(environment, "azure.keyvault.jca.disable-hostname-verification");
    }

    private static boolean environmentPropertyIsTrue(ConfigurableEnvironment environment, String key) {
        return Optional.of(key)
                       .map(environment::getProperty)
                       .map(Boolean::parseBoolean)
                       .orElse(false);
    }
}
