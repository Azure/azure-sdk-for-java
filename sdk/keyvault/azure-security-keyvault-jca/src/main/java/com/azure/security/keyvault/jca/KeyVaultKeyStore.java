// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.certificates.AzureCertificates;
import com.azure.security.keyvault.jca.implementation.certificates.ClasspathCertificates;
import com.azure.security.keyvault.jca.implementation.certificates.JreCertificates;
import com.azure.security.keyvault.jca.implementation.certificates.KeyVaultCertificates;
import com.azure.security.keyvault.jca.implementation.certificates.SpecificPathCertificates;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

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
     * Stores the Jre key store certificates.
     */
    private final JreCertificates jreCertificates;

    /**
     * Store well Know certificates loaded from specific path.
     */
    private final SpecificPathCertificates wellKnowCertificates;

    /**
     * Store custom certificates loaded from specific path.
     */
    private final SpecificPathCertificates customCertificates;

    /**
     * Store certificates loaded from KeyVault.
     */
    private final KeyVaultCertificates keyVaultCertificates;

    /**
     * Store certificates loaded from classpath.
     */
    private final ClasspathCertificates classpathCertificates;

    /**
     * Stores all the certificates.
     */
    private final List<AzureCertificates> allCertificates;

    /**
     * Stores the creation date.
     */
    private final Date creationDate;

    private final boolean refreshCertificatesWhenHaveUnTrustCertificate;

    /**
     * Store the path where the well know certificate is placed
     */
    final String wellKnowPath = Optional.ofNullable(System.getProperty("azure.cert-path.well-known"))
                                        .orElse("/etc/certs/well-known/");

    /**
     * Store the path where the custom certificate is placed
     */
    final String customPath = Optional.ofNullable(System.getProperty("azure.cert-path.custom"))
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
        long refreshInterval = Optional.of("azure.keyvault.jca.certificates-refresh-interval")
                                       .map(System::getProperty)
                                       .map(Long::valueOf)
                                       .orElse(0L);
        refreshCertificatesWhenHaveUnTrustCertificate =
            Optional.of("azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate")
                    .map(System::getProperty)
                    .map(Boolean::parseBoolean)
                    .orElse(false);
        jreCertificates = JreCertificates.getInstance();
        wellKnowCertificates = SpecificPathCertificates.getSpecificPathCertificates(wellKnowPath);
        customCertificates = SpecificPathCertificates.getSpecificPathCertificates(customPath);
        keyVaultCertificates = new KeyVaultCertificates(
            refreshInterval, keyVaultUri, tenantId, clientId, clientSecret, managedIdentity);
        classpathCertificates = new ClasspathCertificates();
        allCertificates = Arrays.asList(
            jreCertificates, wellKnowCertificates, customCertificates, keyVaultCertificates, classpathCertificates);
    }

    /**
     * get key vault key store by system property
     *
     * @return KeyVault key store
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded
     * @throws NoSuchAlgorithmException when algorithm is unavailable.
     * @throws KeyStoreException when no Provider supports a KeyStoreSpi implementation for the specified type
     * @throws IOException when an I/O error occurs.
     */
    public static KeyStore getKeyVaultKeyStoreBySystemProperty() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyVaultJcaProvider.PROVIDER_NAME);
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"),
            System.getProperty("azure.keyvault.managed-identity"));
        keyStore.load(parameter);
        return keyStore;
    }

    @Override
    public Enumeration<String> engineAliases() {
        return Collections.enumeration(getAllAliases());
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineDeleteEntry(String alias) {
        allCertificates.forEach(a -> a.deleteEntry(alias));
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
            keyVaultCertificates.refreshCertificates();
            certificate = keyVaultCertificates.getCertificates().get(alias);
        }
        return certificate;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        String alias = null;
        if (cert != null) {
            List<String> aliasList = getAllAliases();

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
        return getAllAliases().contains(alias);
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter param) {
        if (param instanceof KeyVaultLoadStoreParameter) {
            KeyVaultLoadStoreParameter parameter = (KeyVaultLoadStoreParameter) param;
            keyVaultCertificates.updateKeyVaultClient(parameter.getUri(), parameter.getTenantId(),
                parameter.getClientId(), parameter.getClientSecret(), parameter.getManagedIdentity());
        }
        classpathCertificates.loadCertificatesFromClasspath();
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) {
        classpathCertificates.loadCertificatesFromClasspath();
    }

    private List<String> getAllAliases() {
        List<String> allAliases = new ArrayList<>(jreCertificates.getAliases());
        Map<String, List<String>> aliasLists = new HashMap<>();
        aliasLists.put("well known certificates", wellKnowCertificates.getAliases());
        aliasLists.put("custom certificates", customCertificates.getAliases());
        aliasLists.put("key vault certificates", keyVaultCertificates.getAliases());
        aliasLists.put("class path certificates", classpathCertificates.getAliases());

        aliasLists.forEach((certificatesType, certificates) -> certificates.forEach(alias -> {
            if (allAliases.contains(alias)) {
                LOGGER.log(FINE, String.format("The certificate %s under %s already exists", alias, certificatesType));
            } else {
                allAliases.add(alias);
            }
        }));
        return allAliases;
    }


    @Override
    public void engineSetCertificateEntry(String alias, Certificate certificate) {
        if (getAllAliases().contains(alias)) {
            LOGGER.log(WARNING, "Cannot overwrite own certificate");
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
        return getAllAliases().size();
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) {
    }

    @Override
    public void engineStore(KeyStore.LoadStoreParameter param) {
    }


}
