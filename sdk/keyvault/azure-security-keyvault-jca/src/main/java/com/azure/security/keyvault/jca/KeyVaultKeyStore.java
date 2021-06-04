// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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

import static java.util.logging.Level.INFO;
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
     * Store certificates loaded from classpath.
     */
    private final ClasspathCertificates classpathCertificates;

    /**
     * Store certificates loaded from KeyVault.
     */
    private final KeyVaultCertificates keyVaultCertificates;

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
        allCertificates = Arrays.asList(keyVaultCertificates, classpathCertificates, jreCertificates);
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
        keyVaultCertificates.deleteEntry(alias);
        classpathCertificates.deleteEntry(alias);
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
        loadCertificatesFromClasspath();
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) {
        loadCertificatesFromClasspath();
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate certificate) {
        if (keyVaultCertificates.getAliases().contains(alias)) {
            return;
        }
        engineSetClasspathCertificateEntry(alias, certificate);
    }

    /**
     * Store alias and certificates to Classpath
     * @param alias Classpath certificate's alias
     * @param certificate Classpath certificate
     */
    public void engineSetClasspathCertificateEntry(String alias, Certificate certificate) {
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

    /**
     * Get the filenames.
     *
     * @param path the path.
     * @return the filenames.
     * @throws IOException when an I/O error occurs.
     */
    private String[] getFilenames(String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String resource;
                    while ((resource = br.readLine()) != null) {
                        filenames.add(resource);
                    }
                }
            }
        }
        return filenames.toArray(new String[0]);
    }

    /**
     * Read all the bytes for a given input stream.
     *
     * @param inputStream the input stream.
     * @return the byte-array.
     * @throws IOException when an I/O error occurs.
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        byte[] bytes;
        try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            while (true) {
                int r = inputStream.read(buffer);
                if (r == -1) {
                    break;
                }
                byteOutput.write(buffer, 0, r);
            }
            bytes = byteOutput.toByteArray();
        }
        return bytes;
    }

    /**
     * Side-load certificate from classpath.
     */
    private void loadCertificatesFromClasspath() {
        try {
            String[] filenames = getFilenames("/keyvault");
            if (filenames.length > 0) {
                for (String filename : filenames) {
                    try (InputStream inputStream = getClass().getResourceAsStream("/keyvault/" + filename)) {
                        String alias = filename;
                        if (alias != null) {
                            if (alias.lastIndexOf('.') != -1) {
                                alias = alias.substring(0, alias.lastIndexOf('.'));
                            }
                            byte[] bytes = readAllBytes(inputStream);
                            try {
                                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                                X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                                        new ByteArrayInputStream(bytes));
                                engineSetClasspathCertificateEntry(alias, certificate);
                                LOGGER.log(INFO, "Side loaded certificate: {0} from: {1}",
                                        new Object[]{alias, filename});
                            } catch (CertificateException e) {
                                LOGGER.log(WARNING, "Unable to side-load certificate from: " + filename, e);
                            }
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to determine certificates to side-load", ioe);
        }
    }
}
