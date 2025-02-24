// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaConnectionProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultKeyStoreProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundleProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundlesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.ssl.SslBundleProperties;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.springframework.boot.ssl.SslManagerBundle;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.findNonNullPropertyNames;

/**
 * Azure Key Vault SslBundleRegistrar that registers SSL bundles based Key Vault JCA and Key Vault SSL bundles properties.
 *  @since 5.21.0
 */
public class AzureKeyVaultSslBundlesRegistrar implements SslBundleRegistrar, ResourceLoaderAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureKeyVaultSslBundlesRegistrar.class);
    private ResourceLoader resourceLoader;
    private final AzureKeyVaultJcaProperties jcaProperties;
    private final AzureKeyVaultSslBundlesProperties sslBundlesProperties;
    private static final String KEY_VAULT_PROVIDER_NAME = "AzureKeyVault";
    private static final String[] JCA_SYSTEM_PROPERTY_KEYS = new String[] {
        "azure.keyvault.uri",
        "azure.keyvault.tenant-id",
        "azure.keyvault.client-id",
        "azure.keyvault.client-secret",
        "azure.keyvault.managed-identity",
        "azure.keyvault.jca.certificates-refresh-interval",
        "azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate",
        "azure.cert-path.well-known",
        "azure.cert-path.custom"
    };

    private static final String SKIP_CONFIGURE_REASON_FORMAT = "Skip configuring Key Vault SSL bundles because %s";

    public AzureKeyVaultSslBundlesRegistrar(AzureKeyVaultJcaProperties jcaProperties,
                                            AzureKeyVaultSslBundlesProperties sslBundlesProperties) {
        this.jcaProperties = jcaProperties;
        this.sslBundlesProperties = sslBundlesProperties;
    }

    @Override
    public void registerBundles(SslBundleRegistry registry) {
        if (!isKeyVaultJcaOnClasspath()) {
            LOGGER.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "'com.azure:azure-security-keyvault-jca' doesn't exist in classpath."));
            return;
        }

        final Map<String, AzureKeyVaultSslBundleProperties> azureKeyVault = sslBundlesProperties.getAzureKeyvault();
        if (azureKeyVault.isEmpty()) {
            LOGGER.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "'spring.ssl.bundle.azure-keyvault' is empty."));
            return;
        }

        final AtomicBoolean providerConfigured = new AtomicBoolean(false);
        buildMergedKeyVaultJcaProperties();
        buildMergedSslBundlesProperties();
        final Map<String, AzureKeyVaultJcaConnectionProperties> connections = jcaProperties.getConnections();
        azureKeyVault.forEach((bundleName, bundleProperties) -> {
            if (isKeyVaultPropertiesNotSet(bundleProperties) && isCertificatePathsPropertiesNotSet(bundleProperties)) {
                LOGGER.debug("Skip configuring Key Vault SSL bundle '{}'. At least configure the 'keyvault-ref' of the truststore; "
                    + "or one of 'certificate-paths.custom' and 'certificate-paths.well-known' must be configured.", bundleName);
                return;
            }

            KeyStore keyVaultKeyStore = initilizeKeyVaultKeyStore("keystore", providerConfigured, bundleName,
                connections.get(bundleProperties.getKeystore().getKeyvaultRef()), bundleProperties.getKeystore());
            KeyStore keyVaultTruststore = initilizeKeyVaultKeyStore("truststore", providerConfigured, bundleName,
                connections.get(bundleProperties.getTruststore().getKeyvaultRef()), bundleProperties.getTruststore());
            SslStoreBundle sslStoreBundle = SslStoreBundle.of(keyVaultKeyStore, null, keyVaultTruststore);
            SslBundleKey key = getSslBundleKey(bundleProperties.getKey());
            SslBundle sslBundle = SslBundle.of(sslStoreBundle, key,
                asSslOptions(bundleProperties.getOptions()),
                bundleProperties.getProtocol(),
                SslManagerBundle.from(sslStoreBundle, key));
            registry.registerBundle(bundleName, sslBundle);
            LOGGER.debug("Registered Azure Key Vault SSL bundle '{}'.", bundleName);
        });
    }

    private KeyStore initilizeKeyVaultKeyStore(String storeName,
                                               AtomicBoolean providerConfigured,
                                               String bundleName,
                                               AzureKeyVaultJcaConnectionProperties connectionProperties,
                                               AzureKeyVaultKeyStoreProperties keyStoreProperties) {
        if (connectionProperties == null && !StringUtils.hasText(keyStoreProperties.getCertificatePaths().getCustom())
            && !StringUtils.hasText(keyStoreProperties.getCertificatePaths().getWellKnown())) {
            LOGGER.debug("The {} parameter of Key Vault SSL bundle '{}' is null.", storeName, bundleName);
            return null;
        }

        configureJcaKeyStoreSystemProperties(connectionProperties, keyStoreProperties);
        if (providerConfigured.compareAndSet(false, true)) {
            Security.removeProvider(KEY_VAULT_PROVIDER_NAME);
            Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        }
        KeyStore azureKeyVaultKeyStore;
        try {
            if (hasEmbeddedTomcat()) {
                // DKS (Domain Key Store) type key store can act as a single logical key store and support key stores of various
                // types (JKS - Java Key Store, pkcs12) within a single domain. If you do not use the DKS type, during the handshake,
                // KeyVaultKeyManager.chooseEngineServerAlias is used to find the private key, since Key Vault JCA does not implement
                // this method, it uses the empty method of its superclass and returns null, which ultimately causes the handshake to fail.
                azureKeyVaultKeyStore = KeyStore.getInstance("DKS", KEY_VAULT_PROVIDER_NAME);
            } else {
                azureKeyVaultKeyStore = KeyStore.getInstance(KEY_VAULT_PROVIDER_NAME);
            }
            azureKeyVaultKeyStore.load(null);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to load Key Vault " + storeName + " for SSL bundle '" + bundleName + "'", e);
        }
        return azureKeyVaultKeyStore;
    }

    private boolean isCertificatePathsPropertiesNotSet(AzureKeyVaultSslBundleProperties bundleProperties) {
        return !StringUtils.hasText(bundleProperties.getCertificatePaths().getWellKnown())
            && !StringUtils.hasText(bundleProperties.getCertificatePaths().getCustom());
    }

    private boolean isKeyVaultPropertiesNotSet(AzureKeyVaultSslBundleProperties bundleProperties) {
        return !StringUtils.hasText(bundleProperties.getKeystore().getKeyvaultRef()) && !StringUtils.hasText(bundleProperties.getTruststore().getKeyvaultRef());
    }

    private static SslOptions asSslOptions(SslBundleProperties.Options options) {
        return (options != null) ? SslOptions.of(options.getCiphers(), options.getEnabledProtocols()) : SslOptions.NONE;
    }

    private SslBundleKey getSslBundleKey(SslBundleProperties.Key key) {
        return (key != null) ? SslBundleKey.of(key.getPassword(), key.getAlias()) : SslBundleKey.NONE;
    }

    private void configureJcaKeyStoreSystemProperties(AzureKeyVaultJcaConnectionProperties connectionProperties,
                                                      AzureKeyVaultKeyStoreProperties keyStoreProperties) {
        clearJcaSystemProperties();
        if (connectionProperties != null) {
            putSpringPropertyToSystemProperty(connectionProperties.getEndpoint(), "azure.keyvault.uri");
            putSpringPropertyToSystemProperty(connectionProperties.getProfile().getTenantId(), "azure.keyvault.tenant-id");
            putSpringPropertyToSystemProperty(connectionProperties.getCredential().getClientId(), "azure.keyvault.client-id");
            putSpringPropertyToSystemProperty(connectionProperties.getCredential().getClientSecret(), "azure.keyvault.client-secret");
            if (connectionProperties.getCredential().isManagedIdentityEnabled()) {
                putSpringPropertyToSystemProperty(connectionProperties.getCredential().getClientId(), "azure.keyvault.managed-identity");
            }
        }
        if (keyStoreProperties.getCertificatesRefreshInterval() != null) {
            putSpringPropertyToSystemProperty(String.valueOf(keyStoreProperties.getCertificatesRefreshInterval().toMillis()),
                "azure.keyvault.jca.certificates-refresh-interval");
        }
        putSpringPropertyToSystemProperty(Boolean.toString(keyStoreProperties.isRefreshCertificatesWhenHaveUntrustedCertificate()),
            "azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate");
        putPathToSystemProperty(keyStoreProperties.getCertificatePaths().getWellKnown(), "azure.cert-path.well-known");
        putPathToSystemProperty(keyStoreProperties.getCertificatePaths().getCustom(), "azure.cert-path.custom");
    }

    private void buildMergedKeyVaultJcaProperties() {
        jcaProperties.getConnections().forEach((connectionName, properties) -> {
            BeanUtils.copyProperties(jcaProperties.getCredential(), properties.getCredential(), findNonNullPropertyNames(properties.getCredential()));
            BeanUtils.copyProperties(jcaProperties.getProfile(), properties.getProfile(), findNonNullPropertyNames(properties.getProfile()));
        });
    }

    private void buildMergedSslBundlesProperties() {
        sslBundlesProperties.getAzureKeyvault().forEach((sslBundleName, properties) -> {
            BeanUtils.copyProperties(properties, properties.getKeystore(), findNonNullPropertyNames(properties.getKeystore()));
            BeanUtils.copyProperties(properties, properties.getKeystore().getCertificatePaths(), findNonNullPropertyNames(properties.getKeystore().getCertificatePaths()));
            BeanUtils.copyProperties(properties, properties.getTruststore(), findNonNullPropertyNames(properties.getTruststore()));
            BeanUtils.copyProperties(properties, properties.getTruststore().getCertificatePaths(), findNonNullPropertyNames(properties.getTruststore().getCertificatePaths()));
        });
    }

    private void clearJcaSystemProperties() {
        Arrays.stream(JCA_SYSTEM_PROPERTY_KEYS).forEach(System::clearProperty);
    }

    private void putPathToSystemProperty(String path, String targetKey) {
        Optional.ofNullable(path)
                .filter(p -> p.startsWith("classpath:") || p.startsWith("file:"))
                .map(resourceLoader::getResource)
                .map(res -> {
                    try {
                        return res.getFile();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load the certificate path '" + path + "'", e);
                    }
                })
                .map(File::getAbsolutePath)
                .ifPresentOrElse(absolutePath -> putSpringPropertyToSystemProperty(absolutePath, targetKey),
                    () -> putSpringPropertyToSystemProperty(path, targetKey));

    }

    private boolean hasEmbeddedTomcat() {
        try {
            Class.forName("org.apache.tomcat.InstanceManager");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * The method is used to make the properties in "application.properties" readable in azure-security-keyvault-jca.
     * "application.properties" is analyzed by Spring, and azure-security-keyvault-jca does not depend on Spring.
     * Put the property into System.getProperties() can make them readable in azure-security-keyvault-jca.
     */
    private void putSpringPropertyToSystemProperty(Object propertyValue, String targetKey) {
        if (propertyValue == null) {
            return;
        }

        if (propertyValue instanceof String value) {
            if (!StringUtils.hasText(value)) {
                return;
            }
        }

        System.getProperties().put(targetKey, propertyValue);
    }

    private boolean isKeyVaultJcaOnClasspath() {
        return ClassUtils.isPresent("com.azure.security.keyvault.jca.KeyVaultJcaProvider",
            AzureKeyVaultSslBundlesRegistrar.class.getClassLoader());
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
