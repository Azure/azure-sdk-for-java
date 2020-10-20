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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * The Azure KeyVault implementation of the KeyStoreSpi.
 */
public class KeyVaultKeyStore extends KeyStoreSpi {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyStore.class.getName());

    /**
     * Stores the list of aliases.
     */
    private List<String> aliases;

    /**
     * Stores the certificates by alias.
     */
    private final HashMap<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final HashMap<String, Key> certificateKeys = new HashMap<>();

    /**
     * Stores the creation date.
     */
    private final Date creationDate;

    /**
     * Stores the key vault client.
     */
    private KeyVaultClient keyVault;

    /**
     * Constructor.
     *
     * <p>
     * The constructor uses System.getProperty for
     * <code>azure.keyvault.uri</code>, <code>azure.keyvault.tenantId</code>,
     * <code>azure.keyvault.clientId</code>,
     * <code>azure.keyvault.clientSecret</code> to initialize the keyvault
     * client.
     * </p>
     */
    public KeyVaultKeyStore() {
        creationDate = new Date();
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenantId");
        String clientId = System.getProperty("azure.keyvault.clientId");
        String clientSecret = System.getProperty("azure.keyvault.clientSecret");
        keyVault = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret);
    }

    @Override
    public Enumeration<String> engineAliases() {
        if (aliases == null) {
            aliases = keyVault.getAliases();
        }
        return Collections.enumeration(aliases);
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineDeleteEntry(String alias) {
    }

    @Override
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        return super.engineEntryInstanceOf(alias, entryClass);
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        Certificate certificate;
        if (certificates.containsKey(alias)) {
            certificate = certificates.get(alias);
        } else {
            certificate = keyVault.getCertificate(alias);
            if (certificate != null) {
                certificates.put(alias, certificate);
                if (!aliases.contains(alias)) {
                    aliases.add(alias);
                }
            }
        }
        return certificate;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        String alias = null;
        if (cert != null) {
            if (aliases == null) {
                aliases = keyVault.getAliases();
            }
            for (String candidateAlias : aliases) {
                Certificate certificate = engineGetCertificate(candidateAlias);
                if (certificate.equals(cert)) {
                    alias = candidateAlias;
                    break;
                }
            }
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
        return creationDate;
    }

    @Override
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return super.engineGetEntry(alias, protParam);
    }

    @Override
    public Key engineGetKey(String alias, char[] password) {
        Key key;
        if (certificateKeys.containsKey(alias)) {
            key = certificateKeys.get(alias);
        } else {
            key = keyVault.getKey(alias, password);
            if (key != null) {
                certificateKeys.put(alias, key);
                if (!aliases.contains(alias)) {
                    aliases.add(alias);
                }
            }
        }
        return key;
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        if (aliases == null) {
            aliases = keyVault.getAliases();
        }
        return aliases.contains(alias);
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter param) {
        if (param instanceof KeyVaultLoadStoreParameter) {
            KeyVaultLoadStoreParameter parameter = (KeyVaultLoadStoreParameter) param;
            keyVault = new KeyVaultClient(
                parameter.getUri(),
                parameter.getTenantId(),
                parameter.getClientId(),
                parameter.getClientSecret());
        }
        sideLoad();
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) {
        sideLoad();
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate certificate) {
        if (aliases == null) {
            aliases = keyVault.getAliases();
        }
        if (!aliases.contains(alias)) {
            aliases.add(alias);
            certificates.put(alias, certificate);
        }
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
        return aliases != null ? aliases.size() : 0;
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
        InputStream in = getClass().getResourceAsStream(path);
        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
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
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int r = inputStream.read(buffer);
            if (r == -1) {
                break;
            }
            byteOutput.write(buffer, 0, r);
        }
        return byteOutput.toByteArray();
    }

    /**
     * Side-load certificate from classpath.
     */
    private void sideLoad() {
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
                                engineSetCertificateEntry(alias, certificate);
                                LOGGER.log(INFO, "Side loaded certificate: {0} from: {1}",
                                    new Object[] { alias, filename });
                            } catch (CertificateException e) {
                                LOGGER.log(WARNING, "Unable to side-load certificate", e);
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
