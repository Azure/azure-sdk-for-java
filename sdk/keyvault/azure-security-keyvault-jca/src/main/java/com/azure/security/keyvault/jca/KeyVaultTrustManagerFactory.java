// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Azure Key Vault variant of the TrustManagerFactory.
 */
public class KeyVaultTrustManagerFactory extends TrustManagerFactorySpi {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultTrustManagerFactory.class.getName());

    /**
     * Stores the key managers.
     */
    private final List<TrustManager> trustManagers = new ArrayList<>();

    @Override
    protected void engineInit(KeyStore keystore) {
        LOGGER.entering("KeyVaultTrustManagerFactory", "engineInit", keystore);
        trustManagers.add(new KeyVaultTrustManager(keystore));
    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) {
        /**
         * At least, Tomcat initialises its ssl context's trust manager in this way.
         * If we don't implement this method, the server side "overrideTrustManagerFactory: true" does not work.
         */
        LOGGER.entering("KeyVaultTrustManagerFactory", "engineInit", spec);
        if (spec != null) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
                factory.init(spec);
                trustManagers.add(new KeyVaultTrustManager(factory.getTrustManagers()[0]));
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                LOGGER.log(Level.WARNING, "Unable to get the KeyVaultTrustManagerFactory", e);
            }
        }
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return trustManagers.toArray(new TrustManager[0]);
    }
}
