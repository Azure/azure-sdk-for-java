// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import javax.net.ssl.X509ExtendedKeyManager;
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
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * The Azure Key Vault variant of the X509ExtendedKeyManager.
 */
public class KeyVaultKeyManager extends X509ExtendedKeyManager {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyManager.class.getName());

    /**
     * Stores the keystore.
     */
    private final KeyStore keystore;

    /**
     * Stores the password.
     */
    private final char[] password;

    /**
     * Constructor.
     *
     * @param keystore the keystore.
     * @param password the password.
     */
    public KeyVaultKeyManager(KeyStore keystore, char[] password) {
        LOGGER.entering("KeyVaultKeyManager", "<init>", new Object[] { keystore, password });
        this.keystore = keystore;
        if (password != null) {
            this.password = new char[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        } else {
            this.password = null;
        }
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        LOGGER.entering(
            "KeyVaultKeyManager",
            "chooseClientAlias",
            new Object[] { keyType, issuers, socket }
        );
        String alias = null;
        try {
            /*
             * If we only have one alias and the keystore type is not 
             * 'AzureKeyVault' return that alias as a match.
             */
            if (!keystore.getProvider().getName().equals(KeyVaultJcaProvider.PROVIDER_NAME)
                && keystore.size() == 1) {
                alias = keystore.aliases().nextElement();
            }
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to choose client alias", kse);
        }
        LOGGER.exiting("KeyVaultKeyManager", "chooseClientAlias", alias);
        return alias;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        LOGGER.entering(
            "KeyVaultKeyManager",
            "chooseServerAlias",
            new Object[] { keyType, issuers, socket }
        );
        String alias = null;
        try {
            /*
             * If we only have one alias and the keystore type is not 
             * 'AzureKeyVault' return that alias as a match.
             */
            if (!keystore.getProvider().getName().equals(KeyVaultJcaProvider.PROVIDER_NAME)
                && keystore.size() == 1) {
                alias = keystore.aliases().nextElement();
            }
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to choose server alias", kse);
        }
        LOGGER.exiting("KeyVaultKeyManager", "chooseServerAlias", alias);
        return alias;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        LOGGER.log(INFO, "KeyVaultKeyManager.getClientAliases: {0}, {1}",
            new Object[] { keyType, issuers });
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
        LOGGER.entering("KeyVaultKeyManager", "getCertificateChain", alias);
        List<X509Certificate> chain = new ArrayList<>();
        try {
            Certificate[] keystoreChain = keystore.getCertificateChain(alias);
            if (keystoreChain.length > 0) {
                for (Certificate certificate : keystoreChain) {
                    if (certificate instanceof X509Certificate) {
                        chain.add((X509Certificate) certificate);
                    }
                }
            } else {
                LOGGER.log(WARNING, "No certificate chain found for alias: {0}", alias);
            }
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to get certificate chain for alias: " + alias, kse);
        }
        LOGGER.exiting("KeyVaultKeyManager", "getCertificateChain", chain);
        return chain.toArray(new X509Certificate[0]);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        LOGGER.entering("KeyVaultKeyManager", "getPrivateKey", alias);
        PrivateKey privateKey = null;
        try {
            privateKey = (PrivateKey) keystore.getKey(alias, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            LOGGER.log(WARNING, "Unable to get private key for alias: " + alias, ex);
        }
        LOGGER.exiting("KeyVaultKeyManager", "getPrivateKey", privateKey);
        return privateKey;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        LOGGER.entering("KeyVaultKeyManager", "getServerAliases", new Object[] { keyType, issuers });
        String[] serverAliases = new String[0];
        try {
            serverAliases = Collections.list(keystore.aliases()).toArray(new String[0]);
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to get server aliases", kse);
        }
        LOGGER.exiting("KeyVaultKeyManager", "getServerAliases", serverAliases);
        return serverAliases;
    }
}
