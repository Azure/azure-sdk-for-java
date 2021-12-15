// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The Azure Key Vault variant of the TrustManagerFactory.
 *
 * @see TrustManagerFactorySpi
 */
public final class KeyVaultTrustManagerFactory extends TrustManagerFactorySpi {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultTrustManagerFactory.class.getName());

    /**
     * Stores the key managers.
     */
    private final List<TrustManager> trustManagers = new ArrayList<>();

    /**
     * Engine init.
     *
     * @param keystore the keystore
     */
    @Override
    protected void engineInit(KeyStore keystore) {
        LOGGER.entering("KeyVaultKeyManagerFactory", "engineInit", keystore);
        trustManagers.add(new KeyVaultTrustManager(keystore));
    }


    /**
     * Engine init.
     *
     * @param spec the spec
     */
    @Override
    //TODO: enable create KeyVaultTrustManager with ManagerFactoryParameters
    protected void engineInit(ManagerFactoryParameters spec) {
        LOGGER.entering("KeyVaultKeyManagerFactory", "engineInit", spec);
        trustManagers.add(new KeyVaultTrustManager());
    }

    /**
     * Get trust managers.
     *
     * @return trustManagers the trustManagers
     */
    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return trustManagers.toArray(new TrustManager[0]);
    }
}
