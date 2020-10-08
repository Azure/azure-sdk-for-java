// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;

/**
 * The KeyVault variant of the KeyManagerFactory.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultKeyManagerFactory extends KeyManagerFactorySpi {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyManagerFactory.class.getName());

    /**
     * Stores the key managers.
     */
    private List<KeyManager> keyManagers = new ArrayList<>();
    
    @Override
    protected void engineInit(KeyStore keystore, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        LOGGER.log(INFO, "KeyVaultKeyManagerFactory.engineInit: {0}, {1}", 
                new Object[] {keystore, new String(password)});
        KeyVaultKeyManager manager = new KeyVaultKeyManager(keystore, password);
        keyManagers.add(manager);
    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
    }

    @Override
    protected KeyManager[] engineGetKeyManagers() {
        LOGGER.log(INFO, "KeyVaultKeyManagerFactory.engineGetKeyManagers: {0}", keyManagers); 
        return keyManagers.toArray(new KeyManager[0]);
    }
}
