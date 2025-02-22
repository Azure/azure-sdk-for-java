// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
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
            LOGGER.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "com.azure:azure-security-keyvault-jca doesn't exist in classpath."));
            return;
        }

        final Map<String, AzureKeyVaultSslBundleProperties> azureKeyVault = sslBundlesProperties.getAzureKeyvault();
        if (azureKeyVault.isEmpty()) {
            LOGGER.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "spring.ssl.bundle.azure-keyvault is empty."));
            return;
        }

        final AtomicBoolean providerConfigured = new AtomicBoolean(false);
        buildMergedKeyVaultSslBundleProperties(jcaProperties, azureKeyVault);
        azureKeyVault.forEach((bundleName, bundleProperties) -> {
            if (isSslBundlePropertiesNotSet(bundleProperties) && isCertificatePathsPropertiesNotSet(bundleProperties)) {
                LOGGER.debug("Skip configuring Key Vault SSL bundle {}, because both spring.ssl.bundle.azure-keyvault.{}.endpoint and "
                        + "spring.ssl.bundle.azure-keyvault.{}.key.alias must be configured at the same time; or at least one of spring.ssl.bundle.azure-keyvault.{}.certificate-paths.custom "
                        + "and spring.ssl.bundle.azure-keyvault.{}.certificate-paths.well-known must be configured.",
                    bundleName, bundleName, bundleName, bundleName, bundleName);
                return;
            }

            configureJcaSystemProperties(bundleProperties);
            if (providerConfigured.compareAndSet(false, true)) {
                Security.removeProvider(KEY_VAULT_PROVIDER_NAME);
                Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
            }
            registry.registerBundle(bundleName, getAzureKeyVaultSslBundle(bundleName, bundleProperties));
            LOGGER.debug("Registered Azure Key Vault SSL bundle {}.", bundleName);
        });
    }

    private void buildMergedKeyVaultSslBundleProperties(AzureKeyVaultJcaProperties jcaProperties,
                                                        Map<String, AzureKeyVaultSslBundleProperties> azureKeyVault) {
        azureKeyVault.forEach((bundleName, bundleProperties) -> {
            if (!bundleProperties.isInherit()) {
                return;
            }

            BeanUtils.copyProperties(jcaProperties, bundleProperties, findNonNullPropertyNames(bundleProperties));
            BeanUtils.copyProperties(jcaProperties.getCredential(), bundleProperties.getCredential(), findNonNullPropertyNames(bundleProperties.getCredential()));
            BeanUtils.copyProperties(jcaProperties.getProfile(), bundleProperties.getProfile(), findNonNullPropertyNames(bundleProperties.getProfile()));
        });
    }

    private boolean isCertificatePathsPropertiesNotSet(AzureKeyVaultSslBundleProperties bundleProperties) {
        return !StringUtils.hasText(bundleProperties.getCertificatePaths().getWellKnown())
            && !StringUtils.hasText(bundleProperties.getCertificatePaths().getCustom());
    }

    private boolean isSslBundlePropertiesNotSet(AzureKeyVaultSslBundleProperties bundleProperties) {
        return !StringUtils.hasText(bundleProperties.getEndpoint())
            || (bundleProperties.getKey() == null || !StringUtils.hasText(bundleProperties.getKey().getAlias()));
    }

    private SslBundle getAzureKeyVaultSslBundle(String bundleName, AzureKeyVaultSslBundleProperties bundleProperties) {
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
            throw new RuntimeException("Failed to load Key Vault certificates for bundle " + bundleName, e);
        }
        SslStoreBundle sslStoreBundle = SslStoreBundle.of(azureKeyVaultKeyStore, null, azureKeyVaultKeyStore);
        SslBundleKey key = getSslBundleKey(bundleProperties.getKey());
        return SslBundle.of(sslStoreBundle, key,
            asSslOptions(bundleProperties.getOptions()),
            bundleProperties.getProtocol(),
            SslManagerBundle.from(sslStoreBundle, key));
    }

    private static SslOptions asSslOptions(SslBundleProperties.Options options) {
        return (options != null) ? SslOptions.of(options.getCiphers(), options.getEnabledProtocols()) : SslOptions.NONE;
    }

    private SslBundleKey getSslBundleKey(SslBundleProperties.Key key) {
        return (key != null) ? SslBundleKey.of(key.getPassword(), key.getAlias()) : SslBundleKey.NONE;
    }

    private void configureJcaSystemProperties(AzureKeyVaultSslBundleProperties bundleProperties) {
        clearJcaSystemProperties();
        putSpringPropertyToSystemProperty(bundleProperties.getEndpoint(), "azure.keyvault.uri");
        putSpringPropertyToSystemProperty(bundleProperties.getProfile().getTenantId(), "azure.keyvault.tenant-id");
        putSpringPropertyToSystemProperty(bundleProperties.getCredential().getClientId(), "azure.keyvault.client-id");
        putSpringPropertyToSystemProperty(bundleProperties.getCredential().getClientSecret(), "azure.keyvault.client-secret");
        if (bundleProperties.getCredential().isManagedIdentityEnabled()) {
            putSpringPropertyToSystemProperty(bundleProperties.getCredential().getClientId(), "azure.keyvault.managed-identity");
        }
        if (bundleProperties.getCertificatesRefreshInterval() != null) {
            putSpringPropertyToSystemProperty(String.valueOf(bundleProperties.getCertificatesRefreshInterval().toMillis()),
                "azure.keyvault.jca.certificates-refresh-interval");
        }
        putSpringPropertyToSystemProperty(Boolean.toString(bundleProperties.isRefreshCertificatesWhenHaveUntrustedCertificate()),
            "azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate");
        putPathToSystemProperty(bundleProperties.getCertificatePaths().getWellKnown(), "azure.cert-path.well-known");
        putPathToSystemProperty(bundleProperties.getCertificatePaths().getCustom(), "azure.cert-path.custom");
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
                        throw new RuntimeException("Failed to load the cert path " + path, e);
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
