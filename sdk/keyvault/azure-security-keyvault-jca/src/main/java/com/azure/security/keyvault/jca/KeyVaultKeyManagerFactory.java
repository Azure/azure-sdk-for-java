// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The Azure Key Vault variant of the KeyManagerFactory.
 */
public final class KeyVaultKeyManagerFactory extends KeyManagerFactorySpi {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyManagerFactory.class.getName());

    /**
     * Stores the key managers.
     */
    private final List<KeyManager> keyManagers = new ArrayList<>();

    @Override
    protected void engineInit(KeyStore keystore, char[] password) {
        LOGGER.entering(
            "KeyVaultKeyManagerFactory",
            "engineInit",
            new Object[] { keystore, password }
        );
        KeyVaultKeyManager manager = new KeyVaultKeyManager(keystore, password);
        keyManagers.add(manager);
    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) {
    }

    @Override
    protected KeyManager[] engineGetKeyManagers() {
        LOGGER.exiting("KeyVaultKeyManagerFactory", "engineGetKeyManagers", keyManagers);
        return keyManagers.toArray(new KeyManager[0]);
    }
}
