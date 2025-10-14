// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundleProperties;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Azure Key Vault SslBundleRegistrar that registers SSL bundles based Key Vault JCA properties and Key Vault SSL bundle properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultSslBundleRegistrar implements SslBundleRegistrar, ResourceLoaderAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureKeyVaultSslBundleRegistrar.class);
    private ResourceLoader resourceLoader;
    private final Map<String, AzureKeyVaultJcaProperties.JcaVaultProperties> jcaVaults;
    private final Map<String, AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties> sslBundles;
    private static final String[] JCA_SYSTEM_PROPERTY_KEYS = new String[]{
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

    public AzureKeyVaultSslBundleRegistrar(AzureKeyVaultJcaProperties azureKeyVaultJcaProperties,
                                           AzureKeyVaultSslBundleProperties azureKeyVaultSslBundleProperties) {
        this.jcaVaults = azureKeyVaultJcaProperties.getVaults();
        this.sslBundles = azureKeyVaultSslBundleProperties.getKeyvault();
    }

    @Override
    public void registerBundles(SslBundleRegistry registry) {
        if (!hasKeyVaultJcaOnClasspath()) {
            LOGGER.debug("Skip configuring Key Vault SSL bundles because {}", "'com.azure:azure-security-keyvault-jca' "
                + "doesn't exist in classpath.");
            return;
        }

        if (sslBundles.isEmpty()) {
            LOGGER.debug("Skip configuring Key Vault SSL bundles because {}", "'spring.ssl.bundle.azure-keyvault' "
                + "is empty.");
            return;
        }

        final AtomicBoolean providerConfigured = new AtomicBoolean(false);
        sslBundles.forEach((bundleName, bundle) -> {
            boolean hasAnyCertConfiguredForTruststore = hasAnyCertConfigured(jcaVaults, bundle.getTruststore());
            boolean hasAnyCertConfiguredForKeyStore = hasAnyCertConfigured(jcaVaults, bundle.getKeystore());
            boolean anyCertConfigured = hasAnyCertConfiguredForTruststore || hasAnyCertConfiguredForKeyStore;
            if (!anyCertConfigured) {
                LOGGER.debug("Skip configuring Key Vault SSL bundle '{}'. Consider configuring 'keyvault-ref', "
                    + "'certificate-paths.custom' or 'certificate-paths.well-known' properties of the keystore or "
                    + "truststore.", bundleName);
                return;
            }

            KeyStore keyVaultKeyStore = initilizeKeyVaultKeyStore("keystore",
                bundleName,
                hasAnyCertConfiguredForKeyStore,
                providerConfigured,
                jcaVaults.get(bundle.getKeystore().getKeyvaultRef()),
                bundle.getKeystore());

            KeyStore keyVaultTruststore = initilizeKeyVaultKeyStore("truststore",
                bundleName,
                hasAnyCertConfiguredForTruststore,
                providerConfigured,
                jcaVaults.get(bundle.getTruststore().getKeyvaultRef()),
                bundle.getTruststore());

            SslStoreBundle sslStoreBundle = SslStoreBundle.of(keyVaultKeyStore, null, keyVaultTruststore);

            SslBundleKey sslBundleKey = Optional.ofNullable(bundle.getKey())
                .map(k -> SslBundleKey.of(k.getPassword(), k.getAlias()))
                .orElse(SslBundleKey.NONE);

            SslOptions sslOptions = Optional.ofNullable(bundle.getOptions())
                .map(o -> SslOptions.of(o.getCiphers(), o.getEnabledProtocols()))
                .orElse(SslOptions.NONE);

            SslBundle sslBundle = SslBundle.of(sslStoreBundle, sslBundleKey,
                sslOptions,
                bundle.getProtocol(),
                new KeyVaultSslManagerBundle(sslStoreBundle, sslBundleKey, bundle.isForClientAuth()));

            registry.registerBundle(bundleName, sslBundle);

            LOGGER.debug("Registered Azure Key Vault SSL bundle '{}'.", bundleName);
        });
    }

    private KeyStore initilizeKeyVaultKeyStore(String storeName,
                                               String bundleName,
                                               boolean anyCertConfigured,
                                               AtomicBoolean providerConfigured,
                                               AzureKeyVaultJcaProperties.JcaVaultProperties jcaVaultProperties,
                                               AzureKeyVaultSslBundleProperties.KeyStoreProperties keyStoreProperties) {
        if (!anyCertConfigured) {
            LOGGER.debug("The {} parameter of Key Vault SSL bundle '{}' is null.", storeName, bundleName);
            return null;
        }

        configureJcaKeyStoreSystemProperties(jcaVaultProperties, keyStoreProperties, resourceLoader);
        if (providerConfigured.compareAndSet(false, true)) {
            Security.removeProvider(KeyVaultJcaProvider.PROVIDER_NAME);
            Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        }
        KeyStore azureKeyVaultKeyStore;
        try {
            if (hasEmbeddedTomcat()) {
                // DKS (Domain Key Store) type key store can act as a single logical key store and support key stores of various
                // types (JKS - Java Key Store, pkcs12) within a single domain. When configuring the Tomcat SSL context, if the
                // KeyStore is not of type DKS, the final key store will be reinitialized and loaded, see source code from
                // https://github.com/apache/tomcat/blob/cab38e5b9c4f498336f716afd1bf4161adedd71d/java/org/apache/tomcat/util/net/SSLUtilBase.java#L393~L403,
                // which will result in the KeyManager being used not being wrapped by the JSSEKeyManager provided by Tomcat, see source code from
                // https://github.com/apache/tomcat/blob/cab38e5b9c4f498336f716afd1bf4161adedd71d/java/org/apache/tomcat/util/net/SSLUtilBase.java#L424,
                // so JSSEKeyManager#chooseEngineServerAlias does not delegate KeyVaultKeyManager.chooseEngineServerAlias, resulting in a null return value.
                azureKeyVaultKeyStore = KeyStore.getInstance("DKS", KeyVaultJcaProvider.PROVIDER_NAME);
            } else {
                azureKeyVaultKeyStore = KeyStore.getInstance(KeyVaultJcaProvider.PROVIDER_NAME);
            }
            azureKeyVaultKeyStore.load(null);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to load Key Vault " + storeName + " for SSL bundle '" + bundleName + "'", e);
        }
        return azureKeyVaultKeyStore;
    }

    private static boolean hasKeyVaultJcaOnClasspath() {
        return ClassUtils.isPresent("com.azure.security.keyvault.jca.KeyVaultJcaProvider",
            AzureKeyVaultSslBundleRegistrar.class.getClassLoader());
    }

    private static boolean hasAnyCertConfigured(Map<String, AzureKeyVaultJcaProperties.JcaVaultProperties> jcaVaults,
                                                AzureKeyVaultSslBundleProperties.KeyStoreProperties keyStoreProperties) {
        AzureKeyVaultSslBundleProperties.CertificatePathsProperties certificatePaths = keyStoreProperties.getCertificatePaths();
        String keyvaultRef = keyStoreProperties.getKeyvaultRef();
        boolean localCertConfigured = StringUtils.hasText(certificatePaths.getWellKnown()) || StringUtils.hasText(certificatePaths.getCustom());
        boolean keyVaultRefConfigured = StringUtils.hasText(keyvaultRef) && jcaVaults.get(keyvaultRef) != null;
        return localCertConfigured || keyVaultRefConfigured;
    }

    private static boolean hasEmbeddedTomcat() {
        try {
            Class.forName("org.apache.tomcat.InstanceManager");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private static void configureJcaKeyStoreSystemProperties(AzureKeyVaultJcaProperties.JcaVaultProperties jcaVaultProperties,
                                                             AzureKeyVaultSslBundleProperties.KeyStoreProperties keyStoreProperties,
                                                             ResourceLoader resourceLoader) {
        PropertyMapper pm = new PropertyMapper();
        clearJcaSystemProperties();
        if (jcaVaultProperties != null) {
            pm.from(jcaVaultProperties.getEndpoint())
                .when(StringUtils::hasText)
                .to(v -> System.setProperty("azure.keyvault.uri", v));
            pm.from(jcaVaultProperties.getProfile().getTenantId())
                .when(StringUtils::hasText)
                .to(v -> System.setProperty("azure.keyvault.tenant-id", v));
            String clientId = jcaVaultProperties.getCredential().getClientId();
            pm.from(clientId).when(StringUtils::hasText).to(v -> System.setProperty("azure.keyvault.client-id", v));
            pm.from(jcaVaultProperties.getCredential().getClientSecret())
                .when(StringUtils::hasText)
                .to(v -> System.setProperty("azure.keyvault.client-secret", v));
            pm.from(jcaVaultProperties.getCredential().isManagedIdentityEnabled() && StringUtils.hasText(clientId))
                .whenTrue()
                // When using user managed identity, the client ID must be placed in the managed identity attribute
                .to(v -> System.setProperty("azure.keyvault.managed-identity", clientId));
        }

        pm.from(keyStoreProperties.getCertificatesRefreshInterval())
            .when(Objects::nonNull)
            .to(v -> System.setProperty("azure.keyvault.jca.certificates-refresh-interval", String.valueOf(v.toMillis())));
        pm.from(keyStoreProperties.isRefreshCertificatesWhenHaveUntrustedCertificate())
            .to(v -> System.setProperty("azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate", Boolean.toString(v)));

        pm.from(keyStoreProperties.getCertificatePaths().getWellKnown())
            .to(v -> resolvePath(resourceLoader, v).ifPresent(path -> System.setProperty("azure.cert-path.well-known", path)));
        pm.from(keyStoreProperties.getCertificatePaths().getCustom())
            .to(v -> resolvePath(resourceLoader, v).ifPresent(path -> System.setProperty("azure.cert-path.custom", path)));
    }

    private static void clearJcaSystemProperties() {
        Arrays.stream(JCA_SYSTEM_PROPERTY_KEYS).forEach(System::clearProperty);
    }

    private static Optional<String> resolvePath(ResourceLoader resourceLoader, String path) {
        return Optional.ofNullable(path)
            .filter(p -> p.startsWith("classpath:") || p.startsWith("file:"))
            .map(resourceLoader::getResource)
            .map(res -> {
                try {
                    return res.getFile().getAbsolutePath();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load the certificate path '" + path + "'", e);
                }
            });
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
