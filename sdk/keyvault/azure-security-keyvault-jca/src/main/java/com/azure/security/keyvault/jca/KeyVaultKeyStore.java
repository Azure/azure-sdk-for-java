// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Enumeration;
import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The Azure Key Vault implementation of the KeyStoreSpi.
 */
public final class KeyVaultKeyStore extends KeyStoreSpi {

    /**
     * Stores the key-store name.
     */
    public static final String KEY_STORE_TYPE = "AzureKeyVault";

    /**
     * Stores the algorithm name.
     */
    public static final String ALGORITHM_NAME = KEY_STORE_TYPE;

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyStore.class.getName());

    /**
     * Store certificates loaded from classpath.
     */
    private final ClasspathCertificates classpathCertificates;

    /**
     * Store certificates loaded from KeyVault.
     */
    private final KeyVaultCertificates keyVaultCertificates;

    /**
     * Store custom certificates loaded from file system.
     */
    private final FileSystemCertificates customCertificates;

    /**
     * Store well Know certificates loaded from file system.
     */
    private final FileSystemCertificates wellKnowCertificates;

    /**
     * Stores the Jre key store certificates.
     */
    private final JreCertificates jreCertificates;

    /**
     * Stores all the certificates.
     */
    private final List<AzureCertificates> allCertificates;

    /**
     * Stores the creation date.
     */
    private final Date creationDate;

    /**
     * Stores the key vault client.
     */
    private KeyVaultClient keyVaultClient;

    private final boolean refreshCertificatesWhenHaveUnTrustCertificate;

    private final String customPath = Optional.ofNullable(System.getProperty("azure.cert-path.well-known"))
        .orElse("/etc/certs/well-known/");
    private final String wellKnowPath = Optional.ofNullable(System.getProperty("azure.cert-path.custom"))
        .orElse("/etc/certs/custom/");

    /**
     * Constructor.
     *
     * <p>
     * The constructor uses System.getProperty for
     * <code>azure.keyvault.uri</code>,
     * <code>azure.keyvault.aadAuthenticationUrl</code>,
     * <code>azure.keyvault.tenantId</code>,
     * <code>azure.keyvault.clientId</code>,
     * <code>azure.keyvault.clientSecret</code> and
     * <code>azure.keyvault.managedIdentity</code> to initialize the
     * Key Vault client.
     * </p>
     */
    public KeyVaultKeyStore() {
        creationDate = new Date();
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String managedIdentity = System.getProperty("azure.keyvault.managed-identity");
        if (clientId != null) {
            keyVaultClient = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret);
        } else {
            keyVaultClient = new KeyVaultClient(keyVaultUri, managedIdentity);
        }
        long refreshInterval = Optional.ofNullable(System.getProperty("azure.keyvault.jca.certificates-refresh-interval"))
            .map(Long::valueOf)
            .orElse(0L);
        refreshCertificatesWhenHaveUnTrustCertificate = Optional.ofNullable(System.getProperty("azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate"))
            .map(Boolean::parseBoolean)
            .orElse(false);
        keyVaultCertificates = new KeyVaultCertificates(refreshInterval, keyVaultClient);
        classpathCertificates = new ClasspathCertificates();
        jreCertificates = JreCertificates.getInstance();
        customCertificates = new FileSystemCertificates(customPath);
        wellKnowCertificates = new FileSystemCertificates(wellKnowPath);
        allCertificates = Arrays.asList(keyVaultCertificates, classpathCertificates, jreCertificates, wellKnowCertificates, customCertificates);
    }

    @Override
    public Enumeration<String> engineAliases() {
        List<String> aliasList = allCertificates.stream()
            .map(AzureCertificates::getAliases)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());

        return Collections.enumeration(aliasList);
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineDeleteEntry(String alias) {
        allCertificates.stream().forEach(a -> a.deleteEntry(alias));
    }

    @Override
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        return super.engineEntryInstanceOf(alias, entryClass);
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        Certificate certificate = allCertificates.stream()
            .map(AzureCertificates::getCertificates)
            .filter(a -> a.containsKey(alias))
            .findFirst()
            .map(certificates -> certificates.get(alias))
            .orElse(null);

        if (refreshCertificatesWhenHaveUnTrustCertificate && certificate == null) {
            KeyVaultCertificates.updateLastForceRefreshTime();
            certificate = keyVaultCertificates.getCertificates().get(alias);
        }
        return certificate;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        String alias = null;
        if (cert != null) {
            List<String> aliasList = allCertificates.stream()
                .map(AzureCertificates::getAliases)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

            for (String candidateAlias : aliasList) {
                Certificate certificate = engineGetCertificate(candidateAlias);
                if (certificate.equals(cert)) {
                    alias = candidateAlias;
                    break;
                }
            }
        }
        if (refreshCertificatesWhenHaveUnTrustCertificate && alias == null) {
            alias = keyVaultCertificates.refreshAndGetAliasByCertificate(cert);
        }
        return alias;
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        Certificate[] chain = null;
        Certificate certificate = engineGetCertificate(alias);
        if (certificate != null) {
            chain = new Certificate[1];
            chain[0] = certificate;
        }
        return chain;
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        return new Date(creationDate.getTime());
    }

    @Override
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return super.engineGetEntry(alias, protParam);
    }

    @Override
    public Key engineGetKey(String alias, char[] password) {
        return allCertificates.stream()
                        .map(AzureCertificates::getCertificateKeys)
                        .filter(a -> a.containsKey(alias))
                        .findFirst()
                        .map(certificateKeys -> certificateKeys.get(alias))
                        .orElse(null);
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        return allCertificates.stream()
            .map(AzureCertificates::getAliases)
            .flatMap(Collection::stream)
            .distinct()
            .anyMatch(a -> Objects.equals(a, alias));
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter param) {
        if (param instanceof KeyVaultLoadStoreParameter) {
            KeyVaultLoadStoreParameter parameter = (KeyVaultLoadStoreParameter) param;
            if (parameter.getClientId() != null) {
                keyVaultClient = new KeyVaultClient(
                        parameter.getUri(),
                        parameter.getTenantId(),
                        parameter.getClientId(),
                        parameter.getClientSecret());
            } else if (parameter.getManagedIdentity() != null) {
                keyVaultClient = new KeyVaultClient(
                        parameter.getUri(),
                        parameter.getManagedIdentity()
                );
            } else {
                keyVaultClient = new KeyVaultClient(parameter.getUri());
            }
            keyVaultCertificates.setKeyVaultClient(keyVaultClient);
        }
        classpathCertificates.loadCertificatesFromClasspath();
        wellKnowCertificates.loadCertificatesFromFileSystem();
        customCertificates.loadCertificatesFromFileSystem();
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) {
        classpathCertificates.loadCertificatesFromClasspath();
        wellKnowCertificates.loadCertificatesFromFileSystem();
        customCertificates.loadCertificatesFromFileSystem();
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate certificate) {
        if (keyVaultCertificates.getAliases().contains(alias)) {
            return;
        }
        classpathCertificates.setCertificateEntry(alias, certificate);
    }

    @Override
    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        super.engineSetEntry(alias, entry, protParam);
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {
    }

    @Override
    public int engineSize() {
        return allCertificates.stream()
                     .map(AzureCertificates::getAliases)
                     .flatMap(Collection::stream)
                     .distinct()
                     .collect(Collectors.toList())
                     .size();
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) {
    }

    @Override
    public void engineStore(KeyStore.LoadStoreParameter param) {
    }


}
