// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import javax.net.ssl.X509ExtendedKeyManager;

/**
 * The KeyVault variant of the X509ExtendedKeyManager.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultKeyManager extends X509ExtendedKeyManager {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyManager.class.getName());

    /**
     * Stores the keystore.
     */
    private KeyStore keystore;

    /**
     * Stores the password.
     */
    private char[] password;

    /**
     * Constructor.
     *
     * @param keystore the keystore.
     * @param password the password.
     */
    public KeyVaultKeyManager(KeyStore keystore, char[] password) {
        LOGGER.log(INFO, "KeyVaultKeyManager.<init>: {0}, {1}", 
                new Object[] {keystore, new String(password)});
        this.keystore = keystore;
        this.password = password;
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        LOGGER.log(INFO, "KeyVaultKeyManager.chooseClientAlias: {0}, {1}, {2}", 
                new Object[] {keyType, issuers, socket});
        String alias = null;
        try {
            /*
             * If we only have one alias and the keystore type is not 'AzureKeyVault'
             * return that alias as a match.
             */
            if (!keystore.getProvider().getName().equals("AzureKeyVault")
                    && keystore.size() == 1) {
                alias = keystore.aliases().nextElement();
            }
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to choose client alias", kse);
        }
        LOGGER.log(INFO, "KeyVaultKeyManager.chooseClientAlias: {0}", alias);
        return alias;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        LOGGER.log(INFO, "KeyVaultKeyManager.chooseServerAlias: {0}, {1}, {2}", 
                new Object[] {keyType, issuers, socket});
        String alias = null;
        try {
            /*
             * If we only have one alias and the keystore type is not 'AzureKeyVault'
             * return that alias as a match.
             */
            if (!keystore.getProvider().getName().equals("AzureKeyVault")
                    && keystore.size() == 1) {
                alias = keystore.aliases().nextElement();
            }
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to choose server alias", kse);
        }
        LOGGER.log(INFO, "KeyVaultKeyManager.chooseServerAlias: {0}", alias);
        return alias;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        LOGGER.log(INFO, "KeyVaultKeyManager.getClientAliases: {0}, {1}", 
                new Object[] {keyType, issuers});
        String[] aliases = null;
        try {
            aliases = Collections.list(keystore.aliases()).toArray(new String[0]);
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to get client aliases", kse);
        }
        LOGGER.log(INFO, "KeyVaultKeyManager.getClientAliases: {0}", aliases);
        return aliases;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        LOGGER.log(INFO, "KeyVaultKeyManager.getCertificateChain: {0}", alias);
        List<X509Certificate> chain = new ArrayList<>();
        try {
            Certificate[] keystoreChain = keystore.getCertificateChain(alias);
            if (keystoreChain.length > 0) {
                for (int i = 0; i < keystoreChain.length; i++) {
                    if (keystoreChain[i] instanceof X509Certificate) {
                        chain.add((X509Certificate) keystoreChain[i]);
                    }
                }
            }
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to get certificate chain for alias: " + alias, kse);
        }
        LOGGER.log(INFO, "KeyVaultKeyManager.getCertificateChain: {0}", chain);
        return chain.toArray(new X509Certificate[0]);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        LOGGER.log(INFO, "KeyVaultKeyManager.getPrivateKey: {0}", alias);
        PrivateKey privateKey = null;
        try {
            privateKey = (PrivateKey) keystore.getKey(alias, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            LOGGER.log(WARNING, "Unable to get private key for alias: " + alias, ex);
        }
        LOGGER.log(INFO, "KeyVaultKeyManager.getPrivateKey: {0}", privateKey);
        return privateKey;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        LOGGER.log(INFO, "KeyVaultKeyManager.getServerAliases: {0}, {1}", new Object[] {keyType, issuers});
        String[] serverAliases = new String[0];
        try {
            serverAliases = Collections.list(keystore.aliases()).toArray(new String[0]);
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to get server aliases", kse);
        }
        LOGGER.log(INFO, "KeyVaultKeyManager.getServerAliases: {0}", serverAliases);
        return serverAliases;
    }
}
