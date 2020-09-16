// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * The Azure KeyVault implementation of the KeyStoreSpi.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultKeyStore extends KeyStoreSpi {

    /**
     * Stores the list of aliases.
     */
    private List<String> certificateAliases;

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
     * Stores the key vault.
     */
    private KeyVaultClient keyVault;

    /**
     * Constructor.
     */
    public KeyVaultKeyStore() {
        creationDate = new Date();
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenantId");
        String clientId = System.getProperty("azure.keyvault.clientId", null);
        String clientSecret = System.getProperty("azure.keyvault.clientSecret", null);
        keyVault = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret);        
    }

    @Override
    public Enumeration<String> engineAliases() {
        if (certificateAliases == null) {
            certificateAliases = keyVault.getAliases();
        }
        return Collections.enumeration(certificateAliases);
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
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
                if (!certificateAliases.contains(alias)) {
                    certificateAliases.add(alias);
                }
            }
        }
        return certificate;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        String alias = null;
        if (certificates.containsValue(cert)) {
            for (String candidate : certificates.keySet()) {
                if (certificates.get(candidate).equals(cert)) {
                    alias = candidate;
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
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        Key key;
        if (certificateKeys.containsKey(alias)) {
            key = certificateKeys.get(alias);
        } else {
            key = keyVault.getKey(alias, password);
            if (key != null) {
                certificateKeys.put(alias, key);
                if (!certificateAliases.contains(alias)) {
                    certificateAliases.add(alias);
                }
            }
        }
        return key;
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        if (certificateAliases == null) {
            certificateAliases = keyVault.getAliases();
        }
        return certificateAliases.contains(alias);
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param instanceof KeyVaultLoadStoreParameter) {
            KeyVaultLoadStoreParameter parameter = (KeyVaultLoadStoreParameter) param;
            keyVault = new KeyVaultClient(
                    parameter.getUri(), parameter.getTenantId(),
                    parameter.getClientId(), parameter.getClientSecret());
        }
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    }

    @Override
    public int engineSize() {
        return certificateAliases != null ? certificateAliases.size() : 0;
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
    }
}
