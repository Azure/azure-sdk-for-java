// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.keyvault.certificates.starter;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Order(LOWEST_PRECEDENCE)
public class KeyVaultCertificatesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultCertificatesEnvironmentPostProcessor.class.getName());
    
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

            try {
                Resource[] resources = new PathMatchingResourcePatternResolver()
                        .getResources("classpath:keyvault/*");
                if (resources.length > 0) {
                    try {
                        KeyStore keystore = KeyStore.getInstance("AzureKeyVault");
                        keystore.load(null, null);
                        
                        for (Resource resource : resources) {
                            try (InputStream inputStream = resource.getInputStream()) {
                                String alias = resource.getFilename();
                                if (alias != null) {
                                    alias = alias.substring(0, alias.lastIndexOf('.'));
                                    byte[] bytes = inputStream.readAllBytes();
                                    try {
                                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                                        X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                                                new ByteArrayInputStream(bytes));
                                        keystore.setCertificateEntry(alias, certificate);
                                    } catch (KeyStoreException | CertificateException e) {
                                        LOGGER.log(WARNING, "Unable to side-load certificate", e);
                                    }
                                }
                            }
                        }
                    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                        LOGGER.log(WARNING, "Unable to acquire keystore needed for side-loading", e);
                    }
                }
            } catch (IOException ioe) {
                LOGGER.log(WARNING, "Unable to determine certificates to side-load", ioe);
            }
        }
    }
}
