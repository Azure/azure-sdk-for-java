// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.logging.Level.FINEST;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngine;
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
     */
    public KeyVaultKeyManager(KeyStore keystore, char[] password) {
        this.keystore = keystore;
        this.password = password;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        LOGGER.log(FINEST, "Key type: {0}, issuers: {1}", new Object[]{keyType, issuers});
        return null;
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        LOGGER.log(FINEST, "Key type: {0}, issuers: {1}, socket: {2}", 
                new Object[]{keyType, issuers, socket});
        return null;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        String[] serverAliases = new String[0];
        try {
            serverAliases = Collections.list(keystore.aliases()).toArray(new String[0]);
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
        }
        return serverAliases;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        LOGGER.log(FINEST, "Key type: {0}, issuers: {1}, socket: {2}", 
                new Object[]{keyType, issuers, socket});
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return chain.toArray(new X509Certificate[0]);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        PrivateKey privateKey = null;
        try {
            privateKey = (PrivateKey) keystore.getKey(alias, password);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return privateKey;
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return super.chooseEngineServerAlias(keyType, issuers, engine);
    }
}
