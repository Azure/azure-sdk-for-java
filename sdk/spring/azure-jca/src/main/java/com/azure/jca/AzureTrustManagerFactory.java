// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The Azure Key Vault variant of the TrustManagerFactory.
 */
public class AzureTrustManagerFactory extends TrustManagerFactorySpi {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AzureTrustManagerFactory.class.getName());

    /**
     * Stores the key managers.
     */
    private final List<TrustManager> trustManagers = new ArrayList<>();

    @Override
    protected void engineInit(KeyStore keystore) {
        LOGGER.entering("KeyVaultKeyManagerFactory", "engineInit", keystore);
        trustManagers.add(new AzureTrustManager(keystore));
    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) {
        LOGGER.entering("KeyVaultKeyManagerFactory", "engineInit", spec);
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return trustManagers.toArray(new TrustManager[0]);
    }
}
