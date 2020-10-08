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
     * Stores the key managers.
     */
    private List<KeyManager> keyManagers = new ArrayList<>();
    
    @Override
    protected void engineInit(KeyStore ks, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyVaultKeyManager manager = new KeyVaultKeyManager(ks, password);
        keyManagers.add(manager);
    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
    }

    @Override
    protected KeyManager[] engineGetKeyManagers() {
        return keyManagers.toArray(new KeyManager[0]);
    }
}
