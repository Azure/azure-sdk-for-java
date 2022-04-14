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
import java.security.KeyStore;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.UnrecoverableEntryException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * The Azure Key Vault implementation of the KeyStoreSpi.
 *
 * @see KeyStoreSpi
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
        LOGGER.log(FINE, "Constructing KeyVaultKeyStore.");
        creationDate = new Date();
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String managedIdentity = System.getProperty("azure.keyvault.managed-identity");
        long refreshInterval = getRefreshInterval();
        refreshCertificatesWhenHaveUnTrustCertificate =
            Optional.of("azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate")
                    .map(System::getProperty)
                    .map(Boolean::parseBoolean)
                    .orElse(false);
        jreCertificates = JreCertificates.getInstance();
        LOGGER.log(FINE, String.format("Loaded jre certificates: %s.", jreCertificates.getAliases()));
        wellKnowCertificates = SpecificPathCertificates.getSpecificPathCertificates(wellKnowPath);
        LOGGER.log(FINE, String.format("Loaded well known certificates: %s.", wellKnowCertificates.getAliases()));
        customCertificates = SpecificPathCertificates.getSpecificPathCertificates(customPath);
        LOGGER.log(FINE, String.format("Loaded custom certificates: %s.", customCertificates.getAliases()));
        keyVaultCertificates = new KeyVaultCertificates(
            refreshInterval, keyVaultUri, tenantId, clientId, clientSecret, managedIdentity);
        LOGGER.log(FINE, String.format("Loaded Key Vault certificates: %s.", keyVaultCertificates.getAliases()));
        classpathCertificates = new ClasspathCertificates();
        LOGGER.log(FINE, String.format("Loaded classpath certificates: %s.", classpathCertificates.getAliases()));
        allCertificates = Arrays.asList(
            jreCertificates, wellKnowCertificates, customCertificates, keyVaultCertificates, classpathCertificates);
    }

    Long getRefreshInterval() {
        return Stream.of("azure.keyvault.jca.certificates-refresh-interval-in-ms", "azure.keyvault.jca.certificates-refresh-interval")
                     .map(System::getProperty)
                     .filter(Objects::nonNull)
                     .map(Long::valueOf)
                     .findFirst()
                     .orElse(0L);
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

    /**
     * Lists all the alias names of this keystore.
     *
     * @return enumeration of the alias names
     */
    @Override
    public Enumeration<String> engineAliases() {
        return Collections.enumeration(getAllAliases());
    }

    /**
     * Checks if the given alias exists in this keystore.
     *
     * @param alias the alias name
     * @return true if the alias exists, false otherwise
     */
    @Override
    public boolean engineContainsAlias(String alias) {
        return engineIsCertificateEntry(alias);
    }

    /**
     * Deletes the entry identified by the given alias from this keystore.
     *
     * @param alias the alias name
     */
    @Override
    public void engineDeleteEntry(String alias) {
        allCertificates.forEach(a -> a.deleteEntry(alias));
    }

    /**
     * Determines if the keystore {@code Entry} for the specified
     * {@code alias} is an instance or subclass of the specified
     * {@code entryClass}.
     *
     * @param alias the alias name
     * @param entryClass the entry class
     * @return true if the keystore {@code Entry} for the specified
     *          {@code alias} is an instance or subclass of the
     *          specified {@code entryClass}, false otherwise
     */
    @Override
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        return super.engineEntryInstanceOf(alias, entryClass);
    }

    /**
     * Get the certificate associated with the given alias.
     *
     * @param alias the alias name
     * @return the certificate, or null if the given alias does not exist or
     * does not contain a certificate
     */
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

    /**
     * Get the (alias) name of the first keystore entry whose certificate matches the given certificate.
     *
     * @param cert the certificate to match with.
     * @return the alias name of the first entry with matching certificate,
     * or null if no such entry exists in this keystore
     */
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

    /**
     * Get the certificate chain associated with the given alias.
     *
     * @param alias the alias name
     * @return the certificate chain (ordered with the user's certificate first
     * and the root certificate authority last), or null if the given alias
     * does not exist or does not contain a certificate chain
     */
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

    /**
     * Get the creation date of the entry identified by the given alias.
     *
     * @param alias the alias name
     * @return the creation date of this entry, or null if the given alias does not exist
     */
    @Override
    public Date engineGetCreationDate(String alias) {
        return new Date(creationDate.getTime());
    }

    /**
     * Gets a {@code KeyStore.Entry} for the specified alias with the specified protection parameter.
     *
     * @param alias the alias name
     * @param protParam the protParam
     * @return the {@code KeyStore.Entry} for the specified alias,or {@code null} if there is no such entry
     * @exception KeyStoreException if the operation failed
     * @exception NoSuchAlgorithmException if the algorithm for recovering the entry cannot be found
     * @exception UnrecoverableEntryException if the specified {@code protParam} were insufficient or invalid
     */
    @Override
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return super.engineGetEntry(alias, protParam);
    }

    /**
     * Get key associated with the given alias.
     *
     * @param alias the alias name
     * @param password the password for recovering the key
     * @return the requested key, or null if the given alias does not exist or does not identify a key-related entry
     */
    @Override
    public Key engineGetKey(String alias, char[] password) {
        return allCertificates.stream()
                              .map(AzureCertificates::getCertificateKeys)
                              .filter(a -> a.containsKey(alias))
                              .findFirst()
                              .map(certificateKeys -> certificateKeys.get(alias))
                              .orElse(null);
    }

    /**
     * Check whether the entry identified by the given alias contains a trusted certificate.
     *
     * @param alias the alias name
     * @return true if the entry identified by the given alias contains a trusted certificate, false otherwise
     */
    @Override
    public boolean engineIsCertificateEntry(String alias) {
        return getAllAliases().contains(alias);
    }

    /**
     * Check whether the entry identified by the given alias is a key-related.
     *
     * @param alias the alias for the keystore entry to be checked
     * @return true if the entry identified by the given alias is a key-related, false otherwise
     */
    @Override
    public boolean engineIsKeyEntry(String alias) {
        return engineIsCertificateEntry(alias);
    }

    /**
     * Loads the keystore using the given {@code KeyStore.LoadStoreParameter}.
     *
     * @param param the {@code KeyStore.LoadStoreParameter}
     *          that specifies how to load the keystore,
     *          which may be {@code null}
     */
    @Override
    public void engineLoad(KeyStore.LoadStoreParameter param) {
        if (param instanceof KeyVaultLoadStoreParameter) {
            KeyVaultLoadStoreParameter parameter = (KeyVaultLoadStoreParameter) param;
            keyVaultCertificates.updateKeyVaultClient(parameter.getUri(), parameter.getTenantId(),
                parameter.getClientId(), parameter.getClientSecret(), parameter.getManagedIdentity());
        }
        classpathCertificates.loadCertificatesFromClasspath();
    }

    /**
     * Loads the keystore from the given input stream.
     *
     * @param stream the input stream from which the keystore is loaded,or {@code null}
     * @param password the password
     */
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

    /**
     * Assigns the given certificate to the given alias.
     *
     * @param alias the alias name
     * @param certificate the certificate
     */
    @Override
    public void engineSetCertificateEntry(String alias, Certificate certificate) {
        if (getAllAliases().contains(alias)) {
            LOGGER.log(WARNING, "Cannot overwrite own certificate");
            return;
        }
        classpathCertificates.setCertificateEntry(alias, certificate);
    }

    /**
     * Saves a {@code KeyStore.Entry} under the specified alias.
     * The specified protection parameter is used to protect the
     * {@code Entry}.
     *
     * @param alias the alias name
     * @param entry the entry
     * @param protParam the protParam
     * @throws KeyStoreException if this operation fails
     */
    @Override
    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        super.engineSetEntry(alias, entry, protParam);
    }

    /**
     * Assigns the given key to the given alias, protecting it with the given password.
     *
     * @param alias the alias name
     * @param key the key to be associated with the alias
     * @param password the password to protect the key
     * @param chain the certificate chain
     */
    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
    }

    /**
     * Assigns the given key (that has already been protected) to the given alias.
     *
     * @param alias the alias name
     * @param key the key
     * @param chain the certificate chain
     */
    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {
    }

    /**
     * Retrieves the number of entries in this keystore.
     *
     * @return the number of entries in this keystore
     */
    @Override
    public int engineSize() {
        return getAllAliases().size();
    }

    /**
     * Stores this keystore to the given output stream, and protects its integrity with the given password.
     *
     * @param stream the output stream to which this keystore is written
     * @param password the password to generate the keystore integrity check
     */
    @Override
    public void engineStore(OutputStream stream, char[] password) {
    }

    /**
     * Stores this keystore using the given.
     *
     * @param param the param
     */
    @Override
    public void engineStore(KeyStore.LoadStoreParameter param) {
    }


}
